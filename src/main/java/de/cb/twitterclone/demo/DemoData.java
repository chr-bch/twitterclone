package de.cb.twitterclone.demo;

import de.cb.twitterclone.post.Post;
import de.cb.twitterclone.post.PostRepository;
import de.cb.twitterclone.user.AppUser;
import de.cb.twitterclone.user.AppUserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@Profile("demo")
class DemoData implements ApplicationRunner {

    private static final String CLONE_KEYCLOAK_ID = "6f1c2b9e-3d4a-4c8e-9b7f-2a5d8e1c0f31";

    private final AppUserRepository users;
    private final PostRepository posts;

    DemoData(AppUserRepository users, PostRepository posts) {
        this.users = users;
        this.posts = posts;
    }

    // some AI-generated data for demo purposes
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.findByExternalId("demo-anna").isPresent()) {
            return;
        }
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        AppUser clone = users.findByExternalId(CLONE_KEYCLOAK_ID)
                .orElseGet(() -> users.save(new AppUser(CLONE_KEYCLOAK_ID, "clone", "Clone User", now)));
        AppUser anna = demoUser("anna", "Anna Schmidt", now);
        AppUser jonas = demoUser("jonas", "Jonas Weber", now);
        AppUser lea = demoUser("lea", "Lea Hoffmann", now);
        AppUser mehmet = demoUser("mehmet", "Mehmet Yılmaz", now);

        List<Post> seed = List.of(
                post(anna, now, Duration.ofMinutes(5), "Kaffee ☕ fertig, Tests grün ✅ – der Tag kann kommen."),
                post(mehmet, now, Duration.ofMinutes(12), "Wer hat eigentlich entschieden, dass Montag nach Sonntag kommt?"),
                post(jonas, now, Duration.ofMinutes(34), "Gerade 400 Zeilen gelöscht. Bester Commit der Woche. 🧹"),
                post(clone, now, Duration.ofHours(1), "Hallo Welt! 👋 Mein erster Beitrag hier."),
                post(lea, now, Duration.ofHours(2), "Container sind wie Umzugskartons: Man weiß nie genau, was drin ist, bis man sie aufmacht. 📦"),
                post(anna, now, Duration.ofHours(3), "Code Review Tipp: Erst verstehen, dann kommentieren."),
                post(mehmet, now, Duration.ofHours(4), "Heute gelernt: char_length in Postgres zählt Zeichen, nicht Bytes. 🐘"),
                post(jonas, now, Duration.ofHours(5), "Mittagspause! 🍕"),
                post(lea, now, Duration.ofHours(7), "It works on my machine. Dann liefern wir eben die Maschine mit. 🐳"),
                post(anna, now, Duration.ofHours(9), "Feierabend. Morgen geht's weiter mit dem Frontend."),
                post(clone, now, Duration.ofHours(20), "Keycloak läuft, Login klappt. 🔐"),
                post(mehmet, now, Duration.ofDays(1), "Pro-Tipp: Erst die Tests, dann der Push. Euer Team wird es euch danken."),
                post(jonas, now, Duration.ofDays(1).plusHours(2), "Das Wetter ist zu gut, um drinnen zu sitzen. Also… Laptop auf den Balkon. ☀️"),
                post(lea, now, Duration.ofDays(1).plusHours(5), "Pagination mit Cursor statt Offset: kein doppelter Beitrag beim Blättern mehr."),
                post(anna, now, Duration.ofDays(2), "Neues Lieblings-Emoji: 🦫"),
                post(mehmet, now, Duration.ofDays(2).plusHours(3), "Retro heute: mehr gelacht als diskutiert. Gutes Zeichen."),
                post(jonas, now, Duration.ofDays(2).plusHours(6), "Warum heißt es eigentlich Bug und nicht Feature-in-Disguise? 🐛"),
                post(lea, now, Duration.ofDays(3), "Docker Compose up, Kaffee holen, zurück – alles grün. So soll das sein."),
                post(anna, now, Duration.ofDays(3).plusHours(4), "Lesetipp für heute: RFC 9457. Klingt trocken, ist aber praktisch."),
                post(clone, now, Duration.ofDays(4), "Testbeitrag mit Umlauten: äöü ÄÖÜ ß"),
                post(mehmet, now, Duration.ofDays(4).plusHours(8), "Wochenende! 🎉"),
                post(jonas, now, Duration.ofDays(5), "Kubernetes ist einfach. Sagte niemand, nie."),
                post(lea, now, Duration.ofDays(6), "Erster Tag im neuen Team. Alle super nett! 🙌"),
                post(anna, now, Duration.ofDays(8), "Hallo zusammen, ich bin neu hier. 🌱"));
        posts.saveAll(seed);
    }

    private AppUser demoUser(String username, String displayName, Instant now) {
        return users.save(new AppUser("demo-" + username, username, displayName, now.minus(Duration.ofDays(30))));
    }

    private static Post post(AppUser author, Instant now, Duration ago, String content) {
        return new Post(author, content, now.minus(ago));
    }
}
