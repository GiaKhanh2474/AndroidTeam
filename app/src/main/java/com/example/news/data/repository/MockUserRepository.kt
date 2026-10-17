package com.example.news.data.repository

import com.example.news.data.mock.MockUser
import com.example.news.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Bản MINH HOẠ của [UserRepository]: luôn trả về một hồ sơ bịa, chưa có đăng nhập. */
class MockUserRepository : UserRepository {

    override fun observeProfile(): Flow<UserProfile?> = flowOf(MockUser.profile)
}
