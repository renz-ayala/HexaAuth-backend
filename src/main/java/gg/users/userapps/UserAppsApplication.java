package gg.users.userapps;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class UserAppsApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		dotenv.entries().forEach(dotenvEntry -> System.setProperty(
				dotenvEntry.getKey(),
				dotenvEntry.getValue()
		));
		SpringApplication.run(UserAppsApplication.class, args);
	}

}
