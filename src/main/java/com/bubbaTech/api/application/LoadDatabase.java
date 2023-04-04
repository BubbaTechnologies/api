//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.application;

import com.bubbaTech.api.clothing.ClothType;
import com.bubbaTech.api.clothing.Clothing;
import com.bubbaTech.api.clothing.ClothingRepository;
import com.bubbaTech.api.security.authorities.Authorities;
import com.bubbaTech.api.store.Store;
import com.bubbaTech.api.store.StoreRepository;
import com.bubbaTech.api.user.Gender;
import com.bubbaTech.api.user.User;
import com.bubbaTech.api.user.UserRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@AllArgsConstructor
class LoadDatabase {
    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    private PasswordEncoder passwordEncoder;

    //TODO: link enabled to application.properties profile
    private static final boolean ENABLED = false;

    @Bean
    CommandLineRunner initDataBase(UserRepository userRepository, ClothingRepository clothingRepository, StoreRepository storeRepository) {
        return args -> {
            if (ENABLED) {
                loadUsers(userRepository);
                List<User> users = userRepository.findAll();
                for (User u : users) {
                    log.info(u.toStringBasic());
                }

                Store store = loadStore(storeRepository);

                loadClothing(clothingRepository, store);
                List<Clothing> items = clothingRepository.findAll();
                for (Clothing i : items) {
                    log.info(i.toStringBasic());
                }
            }
        };
    }

    private void loadUsers(UserRepository userRepository) {
        Collection<Authorities> userAuth = new ArrayList<>();
        userAuth.add(new Authorities("USER"));

        User maleTestUser = new User("maleTest@email.com", passwordEncoder.encode("testPassword"), Gender.MALE, userAuth, "Male Test");
        User femaleTestUser = new User("femaleTest@email.com", passwordEncoder.encode("testPassword"), Gender.FEMALE, userAuth, "Female Test");
        User kidTestUser = new User("kidTest@email.com", passwordEncoder.encode("testPassword"), Gender.KID, userAuth, "Kid Test");

        Collection<Authorities> auth = new ArrayList<>();
        auth.add(new Authorities("USER"));
        auth.add(new Authorities("AI"));
        auth.add(new Authorities("SCRAPER"));
        auth.add(new Authorities("ADMIN"));
        User admin = new User("admin", passwordEncoder.encode("lkjh-mnb-Wofny9"), Gender.MALE, auth, "Admin");

        userRepository.save(maleTestUser);
        userRepository.save(femaleTestUser);
        userRepository.save(kidTestUser);
        userRepository.save(admin);
    }

    private void loadClothing(ClothingRepository clothingRepository, Store store) {
        Collection<Gender> g = new ArrayList<>();
        g.add(Gender.MALE);

        Collection<String> imageUrl1 = new ArrayList<>();
        imageUrl1.add("https://www.pacsun.com/dw/image/v2/AAJE_PRD/on/demandware.static/-/Sites-pacsun_storefront_catalog/default/dw88358db9/product_images/0120468680265NEW_00_888.jpg?sw=700");
        Clothing item1 = new Clothing("PacCares Trippy Stripe Embroidered T-Shirt", imageUrl1, "https://www.pacsun.com/paccares/trippy-stripe-embroidered-t-shirt-0120468680265.html", store, ClothType.TOP, g);
        clothingRepository.save(item1);
        imageUrl1.clear();
        imageUrl1.add("https://www.pacsun.com/dw/image/v2/AAJE_PRD/on/demandware.static/-/Sites-pacsun_storefront_catalog/default/dw1f807959/product_images/0120468680264NEW_00_279.jpg?sw=700");
        Clothing item2 = new Clothing("PacSun Vic Colorblock T-Shirt", imageUrl1, "https://www.pacsun.com/pacsun/vic-colorblock-t-shirt-0120468680264.html", store, ClothType.TOP, g);
        clothingRepository.save(item2);
        imageUrl1.clear();
        imageUrl1.add("https://www.pacsun.com/dw/image/v2/AAJE_PRD/on/demandware.static/-/Sites-pacsun_storefront_catalog/default/dw030a93be/product_images/0685604330004NEW_00_089.jpg?sw=700");
        Clothing item3 = new Clothing("Prince x Happy Dad Keychain", imageUrl1, "https://www.pacsun.com/prince/x-happy-dad-keychain-0685604330004.html", store, ClothType.ACCESSORY, g);
        clothingRepository.save(item3);
    }

    private void loadGroup() {

    }

    private Store loadStore(StoreRepository storeRepository) {
        Store newStore = new Store("Pacsun", "https://www.pacsun.com");
        storeRepository.save(newStore);
        return newStore;
    }
}