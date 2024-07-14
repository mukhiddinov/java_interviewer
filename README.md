# InterviewBot

InterviewBot is a Telegram bot designed to assist with interview preparation by providing a curated set of interview questions and answers. It allows users to interactively engage with interview questions based on different modules/topics.

## Features

- **Module Selection:** Choose from various modules/topics to access interview questions.
- **Question and Answer:** View questions and obtain answers interactively.
- **Admin Functionality:** Admins can add new interview questions to existing modules.

## Getting Started

To use InterviewBot, follow these steps:

1. **Prerequisites:**
    - Ensure you have Java JDK installed.
    - Obtain a Telegram bot token from [@BotFather](https://t.me/BotFather).

2. **Installation:**
    - Clone this repository:
      ```
      git clone https://github.com/your-username/InterviewBot.git
      cd InterviewBot
      ```

3. **Configuration:**
    - Replace `BOT_TOKEN_HERE` in `InterviewBot.java` with your Telegram bot token.

4. **Running the Bot:**
    - Compile and run the bot using Maven:
      ```
      mvn compile
      mvn exec:java -Dexec.mainClass="com.example.telegrambot.bot.InterviewBot"
      ```

## Usage

- **Starting the Bot:**
  Start a chat with your bot on Telegram.

- **Commands:**
    - `/start`: Start the bot and receive a welcome message.
    - `/add_question`: (Admins only) Add a new interview question to a module.

- **Module Selection:**
  Use the provided keyboard buttons to select a module/topic.

- **Interacting with Questions:**
    - Select a module to view and interact with random interview questions.
    - Admins can add questions by following the prompts.

## Contributing

Contributions are welcome! If you want to contribute to InterviewBot, follow these steps:
- Fork the repository.
- Create your feature branch (`git checkout -b feature/your-feature`).
- Commit your changes (`git commit -am 'Add some feature'`).
- Push to the branch (`git push origin feature/your-feature`).
- Create a new Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Built with [TelegramBots API](https://github.com/rubenlagus/TelegramBots).
- Inspired by the need for structured interview preparation tools.

## Support

For support, bug reports, or questions, please [open an issue](https://github.com/your-username/InterviewBot/issues).

