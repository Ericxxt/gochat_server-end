package com.ericxxt.gochat;


import com.ericxxt.service.impl.UserServiceImpl;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
class UserServiceTest {

	@Autowired
	private UserServiceImpl userService;

	@Test
	public void test01(){
//		usersService.deleteFriendRequest("1907028CG90247F81","1907028BP34TDXGC1");
	}
}
