package smartStock.mobile

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SmartStockApplication

fun main(args: Array<String>) {
    runApplication<SmartStockApplication>(*args)
}
