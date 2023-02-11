package resa.rodriguez.repositories.user

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import resa.rodriguez.models.User
import java.util.*

/**
 * Repositorio cacheado de usuarios
 *
 * @property repo
 */
@Repository
class UserRepositoryCached
@Autowired constructor(
    private val repo: UserRepository
) : IUserRepositoryCached {
    override suspend fun findAll(): Flow<User> = withContext(Dispatchers.IO) {
        repo.findAll()
    }

    override suspend fun findAllPaged(page: PageRequest): Flow<Page<User>> {
        return repo.findAllBy(page).toList()
            .windowed(page.pageSize, page.pageSize, true)
            .map { PageImpl(it, page, repo.count()) }
            .asFlow()
    }

    override suspend fun findByActivo(activo: Boolean): Flow<User> = withContext(Dispatchers.IO) {
        repo.findAllByActiveOrderByCreatedAt(activo)
    }

    @Cacheable("usuarios")
    override suspend fun findById(id: UUID): User? = withContext(Dispatchers.IO) {
        repo.findById(id)
    }

    @Cacheable("usuarios")
    override suspend fun findByEmail(email: String): User? = withContext(Dispatchers.IO) {
        repo.findFirstByEmailContaining(email).firstOrNull()
    }

    override suspend fun findByUsername(username: String): Flow<User> = withContext(Dispatchers.IO) {
        repo.findByUsernameContaining(username)
    }

    @Cacheable("usuarios")
    override suspend fun findByPhone(phone: String): User? = withContext(Dispatchers.IO) {
        repo.findFirstByPhone(phone).firstOrNull()
    }

    @CachePut("usuarios")
    override suspend fun save(user: User): User = withContext(Dispatchers.IO) {
        repo.save(user)
    }

    @CacheEvict("usuarios")
    override suspend fun deleteById(id: UUID): User? = withContext(Dispatchers.IO) {
        val user = repo.findById(id) ?: return@withContext null
        user.id?.let { repo.deleteById(it) }
        user
    }

    @CachePut("usuarios")
    override suspend fun setActivity(id: UUID, active: Boolean): User? = withContext(Dispatchers.IO) {
        val user = repo.findById(id) ?: return@withContext null
        val res = User(
            id = user.id,
            username = user.username,
            email = user.email,
            password = user.password,
            phone = user.phone,
            avatar = user.avatar,
            role = user.role,
            createdAt = user.createdAt,
            active = active
        )
        repo.save(res)
    }

    @CachePut("usuarios")
    override suspend fun update(id: UUID, user: User): User? = withContext(Dispatchers.IO) {
        val u = repo.findById(id) ?: return@withContext null
        val res = User(
            id = u.id,
            username = user.username,
            email = user.email,
            password = user.password,
            phone = user.phone,
            avatar = user.avatar,
            role = user.role,
            createdAt = u.createdAt,
            active = user.active
        )
        repo.save(res)
    }

    @CachePut("usuarios")
    override suspend fun updateCapado(id: UUID, user: User): User? = withContext(Dispatchers.IO) {
        val u = repo.findById(id) ?: return@withContext null
        val res = User(
            id = u.id,
            username = user.username,
            email = u.email,
            password = user.password,
            phone = user.phone,
            avatar = user.avatar,
            role = u.role,
            createdAt = u.createdAt,
            active = u.active
        )
        repo.save(res)
    }
}