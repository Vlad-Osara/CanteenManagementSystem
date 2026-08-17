package com.canteenbackend.api.admin;

import com.canteenbackend.api.category.model.Category;
import com.canteenbackend.api.category.repository.CategoryRepository;
import com.canteenbackend.api.dish.model.Dish;
import com.canteenbackend.api.dish.repository.DishRepository;
import com.canteenbackend.api.user.model.User;
import com.canteenbackend.api.user.repository.UserRepository;
import com.canteenbackend.helper.base.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DishRepository dishRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        // 1. Khởi tạo tài khoản mặc định
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Quản Trị Viên")
                    .email("admin@canteen.vn")
                    .phoneNumber("0901234567")
                    .role(Role.ADMIN)
                    .balance(new BigDecimal("1000000"))
                    .build());
        }

        if (!userRepository.existsByUsername("staff")) {
            userRepository.save(User.builder()
                    .username("staff")
                    .password(passwordEncoder.encode("staff123"))
                    .fullName("Đầu Bếp Căn Tin")
                    .email("staff@canteen.vn")
                    .phoneNumber("0907654321")
                    .role(Role.STAFF)
                    .balance(BigDecimal.ZERO)
                    .build());
        }

        if (!userRepository.existsByUsername("customer")) {
            userRepository.save(User.builder()
                    .username("customer")
                    .password(passwordEncoder.encode("customer123"))
                    .fullName("Nguyễn Văn Khách")
                    .email("customer@canteen.vn")
                    .phoneNumber("0912345678")
                    .role(Role.CUSTOMER)
                    .balance(new BigDecimal("500000"))
                    .build());
        }

        // 2. Khởi tạo 5 Danh mục chính
        Category catRice = getOrCreateCategory("Cơm & Món Chính", "Các món cơm trưa dinh dưỡng hàng ngày");
        Category catNoodle = getOrCreateCategory("Mì / Bún / Phở", "Các món nước và món sợi đậm vị");
        Category catSnack = getOrCreateCategory("Đồ Ăn Vặt & Bánh", "Bánh mì, gà rán và các món ăn nhanh");
        Category catSide = getOrCreateCategory("Món Thêm & Canh", "Rau luộc, canh thanh nhiệt, đồ ăn kèm");
        Category catDrink = getOrCreateCategory("Đồ Uống & Tráng Miệng", "Nước ép, trà trái cây, cà phê, chè");

        // 3. Danh sách 32 Món ăn Việt Nam hấp dẫn
        List<Dish> dishes = List.of(
                // --- NHÓM 1: CƠM (8 món) ---
                createDish("Cơm gà xối mỡ", "Cơm rang thơm giòn kèm đùi gà chiên da giòn rụm", "35000", "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=800&q=80", catRice),
                createDish("Cơm sườn nướng", "Cơm tấm sườn nướng mật ong thơm lừng kèm đồ chua", "40000", "https://images.unsplash.com/photo-1544025162-d76694265947?w=800&q=80", catRice),
                createDish("Cơm bò lúc lắc", "Thịt bò xào mềm thơm hạt tiêu ăn kèm khoai tây và xà lách", "45000", "https://images.unsplash.com/photo-1534939561126-855b8675edd7?w=800&q=80", catRice),
                createDish("Cơm cá hồi sốt Teriyaki", "Cá hồi nướng sốt ngọt đậm đà phong cách Nhật Bản", "55000", "https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=800&q=80", catRice),
                createDish("Cơm chiên Dương Châu", "Cơm chiên lạp xưởng, tôm nõn, cà rốt và đậu Hà Lan", "35000", "https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=800&q=80", catRice),
                createDish("Cơm cá kho tộ", "Cá basa kho tiêu đậm đà ăn kèm dưa leo rau sống", "35000", "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=800&q=80", catRice),
                createDish("Cơm thịt kho tàu", "Thịt ba chỉ kho trứng cút mềm thơm béo ngậy", "35000", "https://images.unsplash.com/photo-1589302168068-964664d93dc0?w=800&q=80", catRice),
                createDish("Cơm sườn bì chả", "Cơm tấm đầy đủ sườn, chả trứng hấp và bì heo thái sợi", "45000", "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=800&q=80", catRice),

                // --- NHÓM 2: MÌ / BÚN / PHỞ (8 món) ---
                createDish("Phở bò tái nạm", "Phở bò truyền thống nước dùng hầm xương trong 12 tiếng", "45000", "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=800&q=80", catNoodle),
                createDish("Bún thịt nướng", "Bún tươi, thịt nướng than hoa, chả giò, đậu phộng rau thơm", "38000", "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=800&q=80", catNoodle),
                createDish("Bún bò Huế", "Bún bò chuẩn vị Huế cay nồng với bắp bò và chả cua", "45000", "https://images.unsplash.com/photo-1555126634-323283e090fa?w=800&q=80", catNoodle),
                createDish("Mì xào giòn hải sản", "Mì chiên phồng phủ sốt mực, tôm tươi và cải ngọt", "42000", "https://images.unsplash.com/photo-1585032226651-759b368d7246?w=800&q=80", catNoodle),
                createDish("Hủ tiếu Nam Vang", "Hủ tiếu tôm thịt bằm, trứng cút nước dùng thanh ngọt", "40000", "https://images.unsplash.com/photo-1617093727343-374698b1b08d?w=800&q=80", catNoodle),
                createDish("Mì Quảng gà", "Mì Quảng truyền thống kèm thịt gà ta, bánh tráng nướng", "38000", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=800&q=80", catNoodle),
                createDish("Bánh canh cua", "Bánh canh sợi mềm dai với thịt cua tươi và chả cá", "45000", "https://images.unsplash.com/photo-1547592180-85f173990554?w=800&q=80", catNoodle),
                createDish("Mì Udon bò sốt Nhật", "Sợi mì Udon tươi xào thịt bò lát và nấm đông cô", "48000", "https://images.unsplash.com/photo-1618841557871-b4664fbf0cb3?w=800&q=80", catNoodle),

                // --- NHÓM 3: ĂN NHANH & ĂN VẶT (6 món) ---
                createDish("Bánh mì chảo xíu mại", "Bánh mì giòn ăn kèm pate, trứng ốp la và xíu mại sốt cà", "30000", "https://images.unsplash.com/photo-1509722747041-616f39b57569?w=800&q=80", catSnack),
                createDish("Gà rán giòn sốt cay", "2 miếng đùi gà chiên vàng ươm phủ sốt cay ngọt", "35000", "https://images.unsplash.com/photo-1626645738196-c2a7c87a8f58?w=800&q=80", catSnack),
                createDish("Khoai tây chiên lắc phô mai", "Khoai tây chiên giòn tan lắc bột phô mai béo ngậy", "20000", "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=800&q=80", catSnack),
                createDish("Bánh tráng trộn đặc biệt", "Bánh tráng trộn khô bò, xoài xanh, trứng cút và tắc", "20000", "https://images.unsplash.com/photo-1601050690597-df0568f70950?w=800&q=80", catSnack),
                createDish("Gỏi cuốn tôm thịt (3 cuốn)", "Bánh tráng cuốn tôm thịt tươi kèm nước chấm tương đen", "25000", "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?w=800&q=80", catSnack),
                createDish("Bánh bao xá xíu trứng muối", "Bánh bao hấp nóng hổi nhân thịt xá xíu đậm đà", "18000", "https://images.unsplash.com/photo-1563245372-f21724e3856d?w=800&q=80", catSnack),

                // --- NHÓM 4: MÓN THÊM & CANH (4 món) ---
                createDish("Canh chua cá lóc", "Tô canh chua nấu thơm, cà chua, giá đỗ và bạc hà", "25000", "https://images.unsplash.com/photo-1547592180-85f173990554?w=800&q=80", catSide),
                createDish("Canh rong biển đậu hũ thịt bằm", "Canh thanh mát giải nhiệt mùa hè", "20000", "https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?w=800&q=80", catSide),
                createDish("Rau cải thìa xào tỏi", "Rau cải xanh giòn ngọt xào tỏi phi thơm lừng", "15000", "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=800&q=80", catSide),
                createDish("Trứng ốp la thêm (1 quả)", "Trứng gà chiên lòng đào thơm ngon", "8000", "https://images.unsplash.com/photo-1525351484163-7529414344d8?w=800&q=80", catSide),

                // --- NHÓM 5: ĐỒ UỐNG & TRÁNG MIỆNG (6 món) ---
                createDish("Trà đào cam sả", "Trà đào mát lạnh với miếng đào giòn và sả tươi", "25000", "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=800&q=80", catDrink),
                createDish("Cà phê sữa đá Sài Gòn", "Cà phê pha phin truyền thống hòa quyện sữa đặc", "18000", "https://images.unsplash.com/photo-1517701604599-bb29b565090c?w=800&q=80", catDrink),
                createDish("Trà sữa trân châu đường đen", "Trà sữa béo thơm cùng trân châu dai mềm", "28000", "https://images.unsplash.com/photo-1558857563-b37cf006a236?w=800&q=80", catDrink),
                createDish("Nước ép cam nguyên chất", "Cam sành vắt tươi 100% bổ sung vitamin C", "22000", "https://images.unsplash.com/photo-1613478223719-2ab802602423?w=800&q=80", catDrink),
                createDish("Sinh tố bơ sáp", "Bơ sáp béo ngậy xay cùng sữa tươi và sữa đặc", "30000", "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=800&q=80", catDrink),
                createDish("Chè Thái sầu riêng", "Chè hoa quả thanh mát với mít, thạch và sầu riêng tươi", "25000", "https://images.unsplash.com/photo-1563805042-7684c019e1cb?w=800&q=80", catDrink)
        );

        // Lưu toàn bộ món ăn vào CSDL nếu chưa tồn tại
        dishes.stream()
                .filter(dish -> !dishRepository.existsByName(dish.getName()))
                .forEach(dishRepository::save);
    }

    private Dish createDish(String name, String description, String price, String imageUrl, Category category) {
        return Dish.builder()
                .name(name)
                .description(description)
                .price(new BigDecimal(price))
                .imageUrl(imageUrl)
                .isAvailable(true)
                .category(category)
                .build();
    }

    private Category getOrCreateCategory(String name, String description) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> categoryRepository.save(
                        Category.builder()
                                .name(name)
                                .description(description)
                                .build()
                ));
    }
}