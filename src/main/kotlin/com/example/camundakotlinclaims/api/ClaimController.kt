package com.example.camundakotlinclaims.api

import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.variable.Variables
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/claims")
class ClaimController(
    private val runtimeService: RuntimeService,
    private val historyService: HistoryService
) {

    // Isso é como o DTO do spring java normal
    data class ClaimRequest(
        val customerId: String,
        val amount: BigDecimal,
        val type: String
    )

    // Isso é como o DTO do spring java normal
    data class ClaimResponse(
        val processInstanceId: String,
        val businessKey: String?,
        val message: String = "Claim process started successfully",
        val autoApproved: Boolean,
        val amount: BigDecimal? = null
    )

    // Inicia o processo de sinistro
    @PostMapping
    // fun é como public ResponseEntity<ClaimResponse> submitClaim(@RequestBody ClaimRequest body) em Java
    fun submitClaim(@RequestBody body: ClaimRequest): ResponseEntity<ClaimResponse> {

        //val é o equivalente a final em Java e o Variables.createVariables() é o mesmo, só que em Kotlin
        val vars = Variables.createVariables()
            .putValue("customerId", body.customerId)
            .putValue("amount", body.amount)
            .putValue("type", body.type)

        // Cria uma business key única para o processo
        val businessKey = "CLAIM-${body.customerId}-${System.currentTimeMillis()}"

        // o instace é o equivalente a ProcessInstance instance em Java, ele vai iniciar o processo
        // e depois retornar a instância do processo com id e business key
        val instance: ProcessInstance = runtimeService
            .startProcessInstanceByKey(
                // Chave do processo definida no modelo BPMN
                "simple-claim-process",
                // Business key única que criei acima
                businessKey,
                // Variáveis do processo que criei acima, os nomes devem coincidir com os do modelo BPMN
                vars
            )

        // Busca a variável no histórico (porque o processo já terminou)
        // historyService é injetado no construtor da classe, essa classe já é um bean do spring
        val autoApproved = historyService
            .createHistoricVariableInstanceQuery()
            .processInstanceId(instance.id)
                // isso aqui vai buscar a variável que foi setada no processo e retornar o valor que foi setado
            .variableName("autoApproved")
                // singleResult() retorna um único resultado da consulta, é o equivalente a getSingleResult() em Java
            .singleResult()
            ?.value as? Boolean ?: false //se for nulo, retorna false

        return ResponseEntity.ok(
            ClaimResponse(
                processInstanceId = instance.id,
                businessKey = instance.businessKey,
                message = "Claim process started successfully",
                autoApproved = autoApproved,
                amount = if (autoApproved) body.amount else null
            )
        )
    }
}
