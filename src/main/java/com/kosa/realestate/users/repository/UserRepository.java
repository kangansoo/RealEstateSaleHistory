package com.kosa.realestate.users.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.kosa.realestate.users.model.Users;

/**
 * UserDAO 인터페이스
 * 
 * @author 이주윤
 */
@Mapper
@Repository
public interface UserRepository {

  void createUser(Users user);

  Optional<Users> findUserByEmail(String email);

  Users getUserById(Long id);

  List<Users> getUserList();

  int updateUser(String email, String password);

  void deleteUser(Long id);
}
