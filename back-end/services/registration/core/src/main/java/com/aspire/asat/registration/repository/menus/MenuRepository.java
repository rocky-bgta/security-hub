package com.aspire.asat.registration.repository.menus;


import com.aspire.asat.registration.model.menu.Menu;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends MongoRepository<Menu, String> {
    boolean existsByCode(String code);

    List<Menu> findByParentMenuId(String parentMenuId);
}
