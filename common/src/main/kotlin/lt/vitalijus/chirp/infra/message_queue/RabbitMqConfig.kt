package lt.vitalijus.chirp.infra.message_queue

import lt.vitalijus.chirp.domain.events.ChirpEvent
import lt.vitalijus.chirp.domain.events.chat.ChatEventConstants
import lt.vitalijus.chirp.domain.events.user.UserEventConstants
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.JacksonJavaTypeMapper
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import tools.jackson.databind.DefaultTyping
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import tools.jackson.module.kotlin.kotlinModule

@Configuration
@EnableTransactionManagement
class RabbitMqConfig {

    /**
     * Configures Jackson to serialize/deserialize ChirpEvent domain events to/from JSON.
     * Polymorphic serialization allows different event subtypes like UserCreatedEvent, MessageSentEvent to be properly
     * serialized with type information.
     */
    @Bean
    fun messageConverter(): JacksonJsonMessageConverter {
        val polymorphicTypeValidator = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(ChirpEvent::class.java)
            .allowIfSubType("java.util.") // Allow Java lists
            .allowIfSubType("kotlin.collections.") // Kotlin collections
            .build()

        val objectMapper = JsonMapper.builder()
            .addModule(kotlinModule())
            .polymorphicTypeValidator(polymorphicTypeValidator)
            .activateDefaultTyping(polymorphicTypeValidator, DefaultTyping.NON_FINAL)
            .build()

        return JacksonJsonMessageConverter(objectMapper).apply {
            typePrecedence = JacksonJavaTypeMapper.TypePrecedence.TYPE_ID
        }
    }

    /**
     * Sets up the listener factory that processes incoming messages.
     * Transactional: setChannelTransacted(true) ensures messages are processed reliably if processing fails,
     * the message goes back to the queue.
     */
    @Bean
    fun rabbitListenerContainerFactory(
        connectionFactory: ConnectionFactory,
        transactionManager: PlatformTransactionManager,
        messageConverter: JacksonJsonMessageConverter
    ): SimpleRabbitListenerContainerFactory {
        return SimpleRabbitListenerContainerFactory().apply {
            setConnectionFactory(connectionFactory)
            setChannelTransacted(true)
            setTransactionManager(transactionManager)
            setMessageConverter(messageConverter)
        }
    }

    /**
     * RabbitTemplate is used to publish messages to exchanges.
     * Uses the same JSON converter for consistent serialization.
     */
    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        messageConverter: JacksonJsonMessageConverter
    ): RabbitTemplate {
        return RabbitTemplate(connectionFactory).apply {
            this.messageConverter = messageConverter
        }
    }

    /**
     * Routes user-related events like user registration, profile updates.
     */
    @Bean
    fun userExchange() = TopicExchange(
        UserEventConstants.USER_EXCHANGE,
        true,
        false
    )

    /**
     * Routes chat-related events like new messages.
     */
    @Bean
    fun chatExchange() = TopicExchange(
        ChatEventConstants.CHAT_EXCHANGE,
        true,
        false
    )

    @Bean
    fun notificationUserEventsQueue() = Queue(
        MessageQueues.NOTIFICATION_USER_EVENTS,
        true
    )

    @Bean
    fun notificationChatEventsQueue() = Queue(
        MessageQueues.NOTIFICATION_CHAT_EVENTS,
        true
    )

    @Bean
    fun chatUserEventsQueue() = Queue(
        MessageQueues.CHAT_USER_EVENTS,
        true
    )

    @Bean
    fun notificationUserEventsBinding(
        notificationUserEventsQueue: Queue,
        userExchange: TopicExchange
    ): Binding {
        return BindingBuilder
            .bind(notificationUserEventsQueue)
            .to(userExchange)
            .with("user.*")
    }

    @Bean
    fun chatUserEventsBinding(
        chatUserEventsQueue: Queue,
        userExchange: TopicExchange
    ): Binding {
        return BindingBuilder
            .bind(chatUserEventsQueue)
            .to(userExchange)
            .with("user.*")
    }

    @Bean
    fun notificationChatEventsBinding(
        notificationChatEventsQueue: Queue,
        chatExchange: TopicExchange
    ): Binding {
        return BindingBuilder
            .bind(notificationChatEventsQueue)
            .to(chatExchange)
            .with(ChatEventConstants.CHAT_NEW_MESSAGE_KEY)
    }
}
