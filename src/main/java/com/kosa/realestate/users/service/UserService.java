package com.kosa.realestate.users.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kosa.realestate.users.DuplicateUserException;
import com.kosa.realestate.users.UserDTO;
import com.kosa.realestate.users.UserMapper;
import com.kosa.realestate.users.Users;
import com.kosa.realestate.users.repository.UserDAO;
import com.kosa.realestate.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;


/**
 * UserService 클래스
 *
 * @author 이주윤
 */
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserDAO userDAO;
  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;


  // User 생성
  public void createUser(String email, String password, String nickname) {

    Users user = Users.builder().email(email).password(passwordEncoder.encode(password)) // 비밀번호 암호화
        .nickname(nickname).build();

    // user가 작성한 email, nickname이 DB에 있는 데이터와 중복되는지 확인
    try {
      userDAO.createUser(user);
    } catch (DataIntegrityViolationException e) {
      if (e.getMessage().contains("EMAIL_UNIQUE")) {
        throw new DuplicateUserException("Email is already in use.");
      } else if (e.getMessage().contains("NICKNAME_UNIQUE")) {
        throw new DuplicateUserException("Nickname is already in use.");
      } else {
        throw new RuntimeException("An unexpected error occurred.", e);
      }
    }
  }

  // userId로 User 반환
  public UserDTO getUserById(Long userId) {
    Users user = userDAO.getUserById(userId);
    return UserMapper.toDTO(user);
  }

  // 모든 User 반환
  public List<UserDTO> getUserList() {
    List<Users> usersList = userDAO.getUserList();

    return usersList.stream().map(UserMapper::toDTO).collect(Collectors.toList());
  }

  // User 업데이트
  public void updateUser(UserDTO userDTO) {
    userDAO.updateUser(UserMapper.toEntity(userDTO));
  }

  // User 삭제 (soft delete)
  public void deleteUser(Long id) {
    userDAO.deleteUser(id);
  }

  // 이메일 기준 사용자 정보 조회 (MyBatis)
  public UserDTO findUserByEmail(String email) throws UsernameNotFoundException {
    Optional<Users> _user = userDAO.findUserByEmail(email);
    if(_user.isEmpty()) {
      throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
    }

    return UserMapper.toDTO(_user.get()); // Users 객체를 UserDTO 로 변환 후 반환
  }

  // 이메일 기준 사용자 정보 조회 (JPA)
  public UserDTO findByEmail(String email) {

    Users user = userRepository.findByEmail(email)
        .orElseThrow(() -> new DuplicateUserException("not find user"));

    return UserDTO.builder().
        userId(user.getUserId())
        .email(user.getEmail())
        .nickname(user.getNickname())
        .isDeleted(user.getIsDeleted())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }
}
