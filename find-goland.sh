#!/bin/bash

echo "🔍 查找本地GoLand安装..."

# 常见的GoLand安装路径
GOLAND_PATHS=(
    "/Applications/GoLand.app"
    "/Applications/GoLand 2023.3.app"
    "/Applications/GoLand 2023.3.3.app"
    "/Applications/JetBrains/GoLand.app"
    "/Applications/JetBrains/GoLand 2023.3.app"
    "/Applications/JetBrains/GoLand 2023.3.3.app"
    "$HOME/Applications/GoLand.app"
    "$HOME/Applications/GoLand 2023.3.app"
    "$HOME/Applications/GoLand 2023.3.3.app"
)

echo "检查常见安装路径..."
for path in "${GOLAND_PATHS[@]}"; do
    if [ -d "$path" ]; then
        echo "✅ 找到GoLand: $path"
        
        # 检查版本信息
        if [ -f "$path/Contents/Info.plist" ]; then
            version=$(defaults read "$path/Contents/Info.plist" CFBundleShortVersionString 2>/dev/null)
            if [ ! -z "$version" ]; then
                echo "   版本: $version"
            fi
        fi
        
        echo ""
        echo "要使用此GoLand安装，请在gradle.properties中设置："
        echo "localPlatformPath = $path"
        echo ""
        exit 0
    fi
done

echo "❌ 未在常见路径找到GoLand"
echo ""
echo "请手动查找GoLand安装路径："
echo "1. 打开Finder"
echo "2. 搜索 'GoLand'"
echo "3. 右键点击GoLand应用 -> 显示包内容"
echo "4. 复制完整路径"
echo ""
echo "或者使用以下命令搜索："
echo "find /Applications -name '*GoLand*' -type d 2>/dev/null"
echo "mdfind 'kMDItemDisplayName == *GoLand*'"
