package com.example.camundakotlinclaims.delegates

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class RegisterPaymentDelegate : JavaDelegate {

    private val logger = LoggerFactory.getLogger(RegisterPaymentDelegate::class.java)


    // aqui é onde a lógica do delegate acontece, ele pega as variáveis do processo e registra o pagamento
    override fun execute(execution: DelegateExecution) {
        val customerId = execution.getVariable("customerId") as String
        val amount = execution.getVariable("amount") as BigDecimal

        logger.info("Registrando pagamento para cliente={} valor={}", customerId, amount)

        execution.setVariable("paymentRegistered", true)
    }
}
