///usr/bin/env jbang "$0" "$@" ; exit $?

//JAVA 25+
//REPOS mavencentral,spring-milestones=https://repo.spring.io/milestone
//DEPS org.springframework.ai:spring-ai-bedrock-converse:2.0.0-M4
//DEPS org.springframework.ai:spring-ai-client-chat:2.0.0-M4
//DEPS software.amazon.awssdk:bedrockruntime:2.41.34
//DEPS software.amazon.awssdk:auth:2.41.34
//DEPS org.slf4j:slf4j-api:2.0.17
//RUNTIME_OPTIONS -Daws.region=us-west-2

// TODO 1: Add the SLF4J simple logging dependency so you can see what the agent is thinking
//DEPS org.slf4j:slf4j-simple:2.0.17

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.bedrock.converse.BedrockProxyChatModel;
import org.springframework.ai.bedrock.converse.BedrockChatOptions;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

import org.springframework.ai.chat.client.ChatClient;

private static final Logger log = LoggerFactory.getLogger("DungeonMasterSimple");

void main() {
    log.info("=== Starting Dungeon Master AI Agent ===");

    var bearerToken = System.getenv("AWS_BEARER_TOKEN_BEDROCK");
    if (bearerToken == null || bearerToken.isBlank()) {
        log.error("Set AWS_BEARER_TOKEN_BEDROCK first — get your key from the Amazon Bedrock Console → API keys → Short-term API keys");
        return;
    }

    var bedrockClient = BedrockRuntimeClient.builder()
        .region(Region.US_WEST_2)
        .credentialsProvider(AnonymousCredentialsProvider.create())
        .overrideConfiguration(c -> c.putHeader("Authorization", "Bearer " + bearerToken))
        .build();

    var modelId = "us.anthropic.claude-haiku-4-5-20251001-v1:0";
    var options = BedrockChatOptions.builder()
        .model(modelId)
        .build();

    var chatModel = BedrockProxyChatModel.builder()
        .bedrockRuntimeClient(bedrockClient)
        .defaultOptions(options)
        .build();

    // TODO 2: Build a ChatClient with a system prompt that sets the AI personality
    var agent = ChatClient.builder(chatModel)
            .defaultSystem("""
                You are a Dungeon Master (DM) for a Dungeons & Dragons game.
                Your role:
                - Create immersive fantasy worlds and scenarios
                - Narrate scenes vividly with rich descriptions
                - Guide the player through choices and consequences
                - Stay in character at all times

                Rules:
                - Always describe environments, characters, and actions dramatically
                - Offer 2-4 meaningful choices to the player
                - Be creative but coherent
                - Keep responses engaging and concise

                Tone:
                - Mysterious, adventurous, slightly dramatic

                Never break character unless explicitly asked.
                """)
            .build();
    //What is ChatClient?

    // In Spring AI ChatClient, ChatClient is the main interface for interacting with LLMs.

    // Think of it as:

    // A wrapper over your model (chatModel)
    // Handles prompts, context, and responses
    // Lets you define default behavior once, instead of repeating it every time

    // TODO 3: Send a message to the agent and print the response
    try{
        var playerMessage = "I enter the ancient dungeon, torch in hand. What do I see?";
        var response = agent.prompt()
        .user(playerMessage)
        .call()
        .chatResponse();
        log.info("Player Message: {}", playerMessage);
        log.info("DM Response: {}", response);
    } catch (Exception e) {
        log.error("Error occurred while sending message to agent", e);
    }

    log.info("\n=== Ending Dungeon Master AI Agent ===");
}
