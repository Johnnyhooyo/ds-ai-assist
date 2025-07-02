# DS AI Assistant for GoLand

![Build](https://github.com/Johnnyhooyo/ds-ai-assist/workflows/Build/badge.svg)

A smart AI-powered coding assistant designed specifically for GoLand, featuring an intuitive chat interface and seamless integration with DeepSeek AI.

<!-- Plugin description -->
DS AI Assistant for GoLand - A smart AI-powered coding assistant with an intuitive chat interface.

**Features:**
- 🤖 AI-powered chat assistant with DeepSeek integration
- 🎨 Theme-aware UI that follows GoLand's light/dark theme
- 💬 Clean chat interface with floating-style input box and real-time streaming
- ⌨️ Powerful commands: /clear, /newchat, /@file for file attachments
- 📱 Right-side tool window with 80/20 layout (history/input)
- 🔄 Enhanced code blocks with copy buttons and language labels
- 🧠 DeepSeek Reasoner support with reasoning process display

Perfect for Go developers who want an integrated AI assistant directly in their GoLand IDE.
<!-- Plugin description end -->

## ✨ Features

### 🤖 AI-Powered Chat Assistant
- **DeepSeek Integration**: Seamless integration with DeepSeek AI API
- **Real-time Streaming**: Watch AI responses appear in real-time, word by word
- **Reasoning Display**: View AI's thinking process with DeepSeek Reasoner models
- **Multi-turn Conversations**: Maintain context across multiple exchanges

### 💬 Enhanced Chat Interface
- **Clean UI Design**: Floating-style input box with 80/20 layout (history/input)
- **Rich Text Support**: Full Markdown rendering with syntax highlighting
- **Code Block Enhancement**: Dedicated frames for code with copy buttons and language labels
- **Theme Awareness**: Automatically adapts to GoLand's light/dark theme

### ⌨️ Powerful Commands
- `/clear` - Clear current chat history
- `/newchat` - Start a new chat session in a separate tab
- `/@<file_path>` - Attach files to your conversation
- **Keyboard Shortcuts**: Enter to send, Shift+Enter for new lines

### 🎨 User Experience
- **Right-side Tool Window**: Non-intrusive placement in GoLand
- **Auto-complete**: Smart command completion as you type
- **Settings Panel**: Easy configuration of API keys and preferences
- **File Attachments**: Include code files in your conversations

## 📦 Installation

### Method 1: From JetBrains Marketplace (Recommended)
1. Open GoLand
2. Go to <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd>
3. Search for "DS AI Assistant"
4. Click <kbd>Install</kbd>

### Method 2: Manual Installation
1. Download the latest release from [GitHub Releases](https://github.com/Johnnyhooyo/ds-ai-assist/releases/latest)
2. In GoLand, go to <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>
3. Select the downloaded `.zip` file

## ⚙️ Configuration

### 1. Get DeepSeek API Key
1. Visit [DeepSeek Platform](https://platform.deepseek.com/)
2. Sign up or log in to your account
3. Navigate to API Keys section
4. Create a new API key

### 2. Configure the Plugin
1. Open the DS AI Assistant tool window (right sidebar)
2. Click the settings button (⚙️) in the bottom toolbar
3. Enter your DeepSeek API key
4. Configure other preferences:
   - **Model**: Choose your preferred DeepSeek model (default: deepseek-chat)
   - **Temperature**: Control response creativity (0.0-2.0)
   - **Max Tokens**: Set maximum response length
   - **Enable Markdown**: Toggle rich text rendering
   - **Show Reasoning**: Display AI's thinking process (for Reasoner models)

## 🚀 Usage

### Basic Chat
1. Open the DS AI Assistant tool window
2. Type your question in the input box at the bottom
3. Press <kbd>Enter</kbd> to send (or <kbd>Shift+Enter</kbd> for new line)
4. Watch the AI response stream in real-time

### Commands

#### `/clear`
Clear the current chat history
```
/clear
```

#### `/newchat`
Start a new chat session in a separate tab
```
/newchat
```

#### `/@<file_path>`
Attach files to your conversation for context
```
/@src/main.go
/@README.md
/@src/utils/helper.go
```

### File Attachments
- Use the attachment button (📎) in the toolbar
- Or use the `/@` command followed by file path
- Supports relative paths from project root
- AI can analyze and discuss your code


## 🎯 Advanced Features

### DeepSeek Reasoner Integration
When using DeepSeek Reasoner models (like `deepseek-reasoner`):
- **Reasoning Display**: See the AI's step-by-step thinking process
- **Toggle Control**: Enable/disable reasoning display in settings
- **Separate Styling**: Reasoning content appears in gray code blocks

### Code Block Enhancement
- **Language Detection**: Automatic syntax highlighting for code blocks
- **Copy Functionality**: One-click copy button for each code block
- **Language Labels**: Clear indication of programming language
- **Proper Formatting**: Monospace font with appropriate spacing

### Multi-Session Management
- **New Chat Tabs**: Create multiple conversation threads
- **Session Isolation**: Each tab maintains independent context
- **Easy Navigation**: Switch between different conversations

## 🛠️ Troubleshooting

### Common Issues

#### API Key Not Working
- Verify your API key is correct and active
- Check your DeepSeek account has sufficient credits
- Ensure no extra spaces in the API key field

#### Plugin Not Appearing
- Restart GoLand after installation
- Check if the plugin is enabled in Settings > Plugins
- Verify GoLand version compatibility (2023.3+)

#### Streaming Not Working
- Check your internet connection
- Verify firewall settings allow HTTPS connections
- Try switching to a different DeepSeek model

#### Theme Issues
- The plugin automatically adapts to GoLand's theme
- If colors look wrong, try switching GoLand theme and back
- Restart GoLand if theme changes don't apply

## 🤝 Contributing

We welcome contributions! Here's how you can help:

### Development Setup
1. Clone the repository
```bash
git clone https://github.com/Johnnyhooyo/ds-ai-assist.git
cd ds-ai-assist
```

2. Open in IntelliJ IDEA or GoLand
3. Run the plugin in development mode:
```bash
./gradlew runIde
```

### Building
```bash
./gradlew build
```

### Testing
```bash
./gradlew test
```

### Submitting Changes
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Built with [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
- Powered by [DeepSeek AI](https://platform.deepseek.com/)
- Icons and UI components from JetBrains UI Kit

## 📈 Roadmap

### Upcoming Features
- [ ] **Multi-AI Provider Support**: Support for OpenAI, Claude, and other AI providers
- [ ] **Code Generation**: Direct code insertion into editor
- [ ] **Project Context**: Automatic project structure understanding
- [ ] **Custom Prompts**: Save and reuse custom prompt templates
- [ ] **Export Conversations**: Export chat history to various formats
- [ ] **Voice Input**: Speech-to-text for hands-free interaction

### Version History
- **v0.0.1** - Initial release with basic chat functionality

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/Johnnyhooyo/ds-ai-assist/issues)
- **Discussions**: [GitHub Discussions](https://github.com/Johnnyhooyo/ds-ai-assist/discussions)
- **Documentation**: [Wiki](https://github.com/Johnnyhooyo/ds-ai-assist/wiki)

## ⭐ Show Your Support

If you find this plugin helpful, please consider:
- ⭐ Starring the repository
- 🐛 Reporting bugs and issues
- 💡 Suggesting new features
- 🤝 Contributing to the codebase
- 📝 Writing reviews on JetBrains Marketplace

---

**Made with ❤️ for Go developers using GoLand**
