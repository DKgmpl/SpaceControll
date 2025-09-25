package pl.edu.wszib.dnogiec.spacecontroll.init;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data.ConferenceRoomRepository;
import pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data.ReservationRepository;
import pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data.UserRepository;
import pl.edu.wszib.dnogiec.spacecontroll.model.ConferenceRoom;
import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;
import pl.edu.wszib.dnogiec.spacecontroll.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Component
@Profile("dev") //uruchamiaj pod profilem dev
@Order(10)      // po DataInitializer
@RequiredArgsConstructor
public class SimpleDataGenerator implements CommandLineRunner {
    private final ConferenceRoomRepository roomRepo;
    private final UserRepository userRepo;
    private final ReservationRepository reservationRepo;
    private final PasswordEncoder passwordEncoder;


    @Override
    public void run(String... args) {
        // Jeśli są rezerwacje — nie seeduj
        if (reservationRepo.count() > 0) {
            System.out.println("[Seed] Reservations already exist - skipping");
            return;
        }

        Random rnd = new Random(42);

        // 1) Sale
        if (roomRepo.count() == 0) {
            roomRepo.saveAll(List.of(
                    room("A101", "Biurowiec A, 1 piętro", 4, "TV, Tablica"),
                    room("A102", "Biurowiec A, 1 piętro", 6, "Projektor, Tablica"),
                    room("B201", "Biurowiec B, 2 piętro", 8, "TV, Kamera, Mikrofon"),
                    room("C301", "Biurowiec C, 3 piętro", 12, "Projektor 4K, Kamera, Mikrofon"),
                    room("D401", "Biurowiec D, 4 piętro", 20, "Wideokonferencja, 2xTV, Mikrofony sufitowe")
            ));
        }
        List<ConferenceRoom> rooms = roomRepo.findAll();

        // 2) Użytkownicy (poza admin/user)
        ensureUser("u1", "u1@example.com");
        ensureUser("u2", "u2@example.com");
        ensureUser("u3", "u3@example.com");
        ensureUser("u4", "u4@example.com");
        ensureUser("u5", "u5@example.com");
        List<User> users = userRepo.findAll().stream()
                .filter(u -> u.getRole() == User.Role.USER)
                .toList();

        // 3) Parametry generacji
        LocalDate startDate = LocalDate.now().minusDays(28);
        LocalDate endDate = LocalDate.now().plusDays(7);
        LocalTime dayStart = LocalTime.of(8, 0);
        LocalTime dayEnd = LocalTime.of(18, 0);
        int[] possibleDurationsMin = {30, 45, 60, 90, 120};

        // 4) Generuj
        int created = 0;
        for (ConferenceRoom room : rooms) {
            for (LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
                // Dzienny harmonogram bez kolizji (lista przedziałów)
                List<TimeRange> dayRanges = new ArrayList<>();

                int meetingsToday = 2 + rnd.nextInt(5); // 2..6
                for (int i = 0; i < meetingsToday; i++) {
                    // losowy slot
                    LocalDateTime start = randomStart(rnd, day, dayStart, dayEnd.minusMinutes(30));
                    int duration = possibleDurationsMin[rnd.nextInt(possibleDurationsMin.length)];
                    LocalDateTime end = start.plusMinutes(duration);

                    // upewnij się, że w obrębie dnia i w godzinach pracy
                    if (end.toLocalDate().isAfter(day) || end.toLocalTime().isAfter(dayEnd)) continue;

                    // brak kolizji z dotychczasowymi tego dnia
                    if (overlapsAny(dayRanges, start, end)) continue;

                    // losowy user
                    User user = users.get(rnd.nextInt(users.size()));

                    // status w zależności od czasu
                    LocalDateTime now = LocalDateTime.now();
                    Reservation.ReservationStatus status;
                    if (end.isBefore(now)) {
                        status = Reservation.ReservationStatus.COMPLETED;
                    } else if (start.isAfter(now)) {
                        // część anulujemy, część pozostawiamy aktywną
                        status = rnd.nextDouble() < 0.15 ? Reservation.ReservationStatus.CANCELLED : Reservation.ReservationStatus.ACTIVE;
                    } else {
                        status = Reservation.ReservationStatus.ACTIVE;
                    }

                    // expectedAttendees: 50–120% pojemności (czasem lekkie niedoszacowanie/oversubscription)
                    int cap = room.getCapacity();
                    int expected = Math.max(1, (int) Math.round(cap * (0.5 + rnd.nextDouble() * 0.7)));
                    // dla przyszłych ACTIVE przytnij do capacity (by nie psuć UX), dla historycznych zostaw czasem >cap
                    if (!end.isBefore(now) && status == Reservation.ReservationStatus.ACTIVE) {
                        expected = Math.min(expected, cap);
                    }

                    Reservation r = new Reservation();
                    r.setConferenceRoom(room);
                    r.setUser(user);
                    r.setStartTime(start);
                    r.setEndTime(end);
                    r.setStatus(status);
                    r.setNotes(null);
                    r.setExpectedAttendees(expected);

                    // Rozkład lead time (wyprzedzenia) w godzinach
                    // 10%: tego samego dnia (0–2h wcześniej)
                    // 30%: 1–2 dni wcześniej (24–48h)
                    // 40%: 3–7 dni wcześniej (72–168h)
                    // 20%: 8–30 dni wcześniej (192–720h)
                    double p = rnd.nextDouble();
                    int hoursBack;
                    if (p < 0.1) {
                        hoursBack = rnd.nextInt(3); // 0..2h
                    } else if (p < 0.4) {
                        hoursBack = 24 + rnd.nextInt(25); // 24..48h
                    } else if (p < 0.8) {
                        hoursBack = 72 + rnd.nextInt(97); // 72..168h
                    } else {
                        hoursBack = 192 + rnd.nextInt(529); // 192..720h
                    }
                    // createdAt = startTime - hoursBack
                    r.setCreatedAt(r.getStartTime().minusHours(hoursBack));

                    if (status == Reservation.ReservationStatus.CANCELLED) {
                        // anulacja zwykle przed startem: 1–48h wcześniej
                        r.setCancelledAt(r.getStartTime().minusHours(1 + rnd.nextInt(48)));
                    } else if (status == Reservation.ReservationStatus.COMPLETED) {
                        // 70% completed ma check-in
                        if (rnd.nextDouble() < 0.7) {
                            r.setCheckInTime(r.getStartTime().minusMinutes(5 + rnd.nextInt(10)));
                        }
                    }

                    // zapisz
                    reservationRepo.save(r);
                    dayRanges.add(new TimeRange(start, end));
                    created++;
                }
            }
        }
        System.out.println("[Seed] Created reservations: " + created);
    }

    private ConferenceRoom room(String name, String location, int capacity, String equipment) {
        ConferenceRoom r = new ConferenceRoom();
        r.setName(name);
        r.setLocation(location);
        r.setCapacity(capacity);
        r.setEquipment(equipment);
        return r;
    }

    private void ensureUser(String login, String email) {
        String l = login.toLowerCase(Locale.ROOT).trim();
        String e = email.toLowerCase(Locale.ROOT).trim();
        if (userRepo.findByLogin(l).isPresent()) return;

        User u = new User();
        u.setLogin(l);
        u.setEmail(e);
        u.setName(l.toUpperCase(Locale.ROOT));
        u.setSurname("Demo");
        u.setPassword(passwordEncoder.encode("user123"));
        u.setRole(User.Role.USER);
        userRepo.save(u);
    }

    private static LocalDateTime randomStart(Random rnd, LocalDate day, LocalTime from, LocalTime to) {
        int fromMin = from.getHour() * 60 + from.getMinute();
        int toMin = to.getHour() * 60 + to.getMinute();
        int minuteOfDay = fromMin + rnd.nextInt(Math.max(1, toMin - fromMin));
        return day.atTime(minuteOfDay / 60, minuteOfDay % 60);
    }

    private static boolean overlapsAny(List<TimeRange> ranges, LocalDateTime s, LocalDateTime e) {
        for (TimeRange r : ranges) {
            if (r.start.isBefore(e) && r.end.isAfter(s)) return true;
        }
        return false;
    }

    private record TimeRange(LocalDateTime start, LocalDateTime end) {
    }

}
