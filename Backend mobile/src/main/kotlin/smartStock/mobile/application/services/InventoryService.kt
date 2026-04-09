package smartStock.mobile.application.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import smartStock.mobile.application.dtos.InventoryResponse
import smartStock.mobile.application.dtos.InventoryUpdateRequest
import smartStock.mobile.application.dtos.MovementStockRequest
import smartStock.mobile.application.interfaces.IInventoryRepository
import smartStock.mobile.application.interfaces.IMovementRepository
import smartStock.mobile.application.interfaces.IProductRepository
import smartStock.mobile.domain.entities.Inventory
import smartStock.mobile.domain.entities.Movement

@Service
class InventoryService(
    private val inventoryRepository: IInventoryRepository,
    private val movementRepository: IMovementRepository,
    private val productRepository: IProductRepository,
) {
    fun getAll(): List<InventoryResponse> {
        val productsById = productRepository.findAll().associateBy { it.id }
        return inventoryRepository.findAll().map { inventory ->
            val productName = productsById[inventory.productId]?.name
            inventory.toResponse(productName)
        }
    }

    fun getById(id: Int): InventoryResponse? {
        return inventoryRepository.findById(id).map { inventory ->
            val productName = productRepository.findById(inventory.productId).map { it.name }.orElse(null)
            inventory.toResponse(productName)
        }.orElse(null)
    }

    fun getByProductId(productId: Int): InventoryResponse? {
        return inventoryRepository.findByProductId(productId).map { inventory ->
            val productName = productRepository.findById(inventory.productId).map { it.name }.orElse(null)
            inventory.toResponse(productName)
        }.orElse(null)
    }

    fun update(
        id: Int,
        request: InventoryUpdateRequest,
    ): InventoryResponse {
        val existingInventory = inventoryRepository.findById(id).orElseThrow { RuntimeException("Inventario no encontrado") }

        val updatedInventory =
            existingInventory.copy(
                location = request.location,
                reorderPoint = request.reorderPoint,
            )
        val savedInventory = inventoryRepository.save(updatedInventory)
        val productName = productRepository.findById(savedInventory.productId).map { it.name }.orElse(null)
        return savedInventory.toResponse(productName)
    }

    fun activate(id: Int) {
        val existingInventory = inventoryRepository.findById(id).orElseThrow { RuntimeException("Inventario no encontrado") }
        val updatedInventory = existingInventory.copy(status = true)
        inventoryRepository.save(updatedInventory)
    }

    fun deactivate(id: Int) {
        val existingInventory = inventoryRepository.findById(id).orElseThrow { RuntimeException("Inventario no encontrado") }
        val updatedInventory = existingInventory.copy(status = false)
        inventoryRepository.save(updatedInventory)
    }

    @Transactional
    fun registerEntry(request: MovementStockRequest): Int {
        val inventory =
            inventoryRepository.findByProductId(request.productId)
                .orElseGet {
                    // Si no existe inventario para el producto, lo creamos
                    inventoryRepository.save(
                        Inventory(
                            productId = request.productId,
                            quantity = 0,
                        ),
                    )
                }

        // Actualizar stock
        val newQuantity = inventory.quantity + request.quantity
        val updatedInventory = inventory.copy(quantity = newQuantity)
        inventoryRepository.save(updatedInventory)

        // Registrar movimiento
        val movement =
            Movement(
                inventoryId = updatedInventory.id!!,
                userId = request.userId,
                type = "ENTRADA",
                quantity = request.quantity,
                batch = request.batch,
                status = true,
            )
        movementRepository.save(movement)

        return newQuantity
    }

    @Transactional
    fun registerExit(request: MovementStockRequest): Int {
        val inventory =
            inventoryRepository.findByProductId(request.productId)
                .orElseThrow { RuntimeException("Inventario no encontrado para el producto ${request.productId}") }

        if (inventory.quantity < request.quantity) {
            throw RuntimeException("Stock insuficiente. Disponible: ${inventory.quantity}")
        }

        // Actualizar stock
        val newQuantity = inventory.quantity - request.quantity
        val updatedInventory = inventory.copy(quantity = newQuantity)
        inventoryRepository.save(updatedInventory)

        // Registrar movimiento (Puede ser SALIDA o AVERIA, lo tomamos del request o default SALIDA)
        val type = if (request.type.isNotBlank()) request.type else "SALIDA"

        val movement =
            Movement(
                inventoryId = updatedInventory.id!!,
                userId = request.userId,
                type = type,
                quantity = request.quantity,
                batch = request.batch,
                status = true,
            )
        movementRepository.save(movement)

        return newQuantity
    }

    private fun Inventory.toResponse(productName: String?) =
        InventoryResponse(
            id = id,
            productId = productId,
            productName = productName,
            location = location,
            quantity = quantity,
            reorderPoint = reorderPoint,
            status = status,
        )
}
