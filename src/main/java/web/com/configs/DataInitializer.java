package web.com.configs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import web.com.entity.User;
import web.com.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@gmail.com";
            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User admin = new User();
                admin.setFullName("Administrator");
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("123456"));
                admin.setImages("https://i.pravatar.cc/150?img=12");
                userRepository.save(admin);
                System.out.println("==================================================");
                System.out.println(">>> Khởi tạo tài khoản mặc định:");
                System.out.println(">>> Email: " + adminEmail);
                System.out.println(">>> Mật khẩu: 123456");
                System.out.println("==================================================");
            }

            String teacherEmail = "trungnh@hcmute.edu.vn";
            if (userRepository.findByEmail(teacherEmail).isEmpty()) {
                User teacher = new User();
                teacher.setFullName("ThS. Nguyễn Hữu Trung");
                teacher.setEmail(teacherEmail);
                teacher.setPassword(passwordEncoder.encode("123456"));
                teacher.setImages("https://i.pravatar.cc/150?img=68");
                userRepository.save(teacher);
            }
        };
    }
}
