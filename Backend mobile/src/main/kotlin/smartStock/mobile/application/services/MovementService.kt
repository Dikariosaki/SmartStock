package smartStock.mobile.application.services

import org.springframework.stereotype.Service
import smartStock.mobile.application.dtos.MovementResponse
import smartStock.mobile.application.interfaces.IInventoryRepository
import smartStock.mobile.application.interfaces.IMovementRepository
import smartStock.mobile.application.interfaces.IProductRepository
import smartStock.mobile.application.interfaces.IUserRepository
import smartStock.mobile.domain.entities.Movement

@Service
class MovementService(
    private val movementRepository: IMovementRepository,
    private val inventoryRepository: IInventoryRepository,
    private val productRepository: IProductRepository,
    private val userRepository: IUserRepository,
) {
    fun getAll(): List<MovementResponse> {
        val inventoriesById = inventoryRepository.findAll().associateBy { it.id }
        val productsById = productRepository.findAll().associateBy { it.id }
        val usersById = userRepository.findAll().associateBy { it.id }

        return movementRepository.findAll().map { movement ->
            val inventory = inventoriesById[movement.inventoryId]
            val productName = inventory?.productId?.let { productsById[it]?.name }
            val inventoryLocation = inventory?.location
            val userName = usersById[movement.userId]?.name
            movement.toResponse(userName, productName, inventoryLocation)
        }
    }

    fun getByInventoryId(inventoryId: Int): List<MovementResponse> {
        val inventory = inventoryRepository.findById(inventoryId).orElse(null)
        val productName =
            inventory?.productId?.let {
                productRepository.findById(it).map { product -> product.name }.orElse(null)
            }
        val inventoryLocation = inventory?.location
        val usersById = userRepository.findAll().associateBy { it.id }

        return movementRepository.findByInventoryId(inventoryId).map { movement ->
            val userName = usersById[movement.userId]?.name
            movement.toResponse(userName, productName, inventoryLocation)
        }
    }

    private fun Movement.toResponse(
        userName: String?,
        productName: String?,
        inventoryLocation: String?,
    ) = 
        MovementResponse(
            id = id,
            inventoryId = inventoryId,
            userId = userId,
            userName = userName,
            productName = productName,
            inventoryLocation = inventoryLocation,
            type = type,
            quantity = quantity,
            date = date,
            batch = batch,
            status = status,
        )
}
