package com.github.johnnyhooyo.dsaiassist.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 附件管理器 - 管理聊天中的文件附件
 */
public class AttachmentManager {
    
    private final Project project;
    private final List<AttachedFile> attachedFiles = new ArrayList<>();
    
    public AttachmentManager(Project project) {
        this.project = project;
    }
    
    /**
     * 添加文件附件
     */
    public boolean addFile(String filePath) {
        if (project == null || filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        
        try {
            VirtualFile file = project.getBaseDir().findFileByRelativePath(filePath);
            if (file != null && file.exists()) {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                if (psiFile != null) {
                    AttachedFile attachedFile = new AttachedFile(filePath, file, psiFile);
                    
                    // 检查是否已经添加
                    if (!attachedFiles.contains(attachedFile)) {
                        attachedFiles.add(attachedFile);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * 移除文件附件
     */
    public boolean removeFile(String filePath) {
        return attachedFiles.removeIf(file -> file.getFilePath().equals(filePath));
    }
    
    /**
     * 清空所有附件
     */
    public void clearAll() {
        attachedFiles.clear();
    }
    
    /**
     * 获取所有附件
     */
    public List<AttachedFile> getAttachedFiles() {
        return new ArrayList<>(attachedFiles);
    }
    
    /**
     * 获取附件数量
     */
    public int getAttachmentCount() {
        return attachedFiles.size();
    }
    
    /**
     * 检查是否有附件
     */
    public boolean hasAttachments() {
        return !attachedFiles.isEmpty();
    }
    
    /**
     * 生成附件内容用于发送给AI
     */
    public String generateAttachmentContent() {
        if (attachedFiles.isEmpty()) {
            return "";
        }
        
        StringBuilder content = new StringBuilder();
        content.append("\n\n--- 附件文件 ---\n");
        
        for (AttachedFile file : attachedFiles) {
            content.append("\n文件: ").append(file.getFilePath()).append("\n");
            content.append("```").append(getFileExtension(file.getFilePath())).append("\n");
            
            try {
                String fileContent = file.getPsiFile().getText();
                // 限制文件内容长度，避免超出token限制
                if (fileContent.length() > 2000) {
                    fileContent = fileContent.substring(0, 2000) + "\n... (文件内容过长，已截断)";
                }
                content.append(fileContent);
            } catch (Exception e) {
                content.append("无法读取文件内容: ").append(e.getMessage());
            }
            
            content.append("\n```\n");
        }
        
        return content.toString();
    }
    
    /**
     * 获取文件扩展名用于代码高亮
     */
    private String getFileExtension(String filePath) {
        int lastDot = filePath.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filePath.length() - 1) {
            return filePath.substring(lastDot + 1);
        }
        return "";
    }
    
    /**
     * 附件文件类
     */
    public static class AttachedFile {
        private final String filePath;
        private final VirtualFile virtualFile;
        private final PsiFile psiFile;
        
        public AttachedFile(String filePath, VirtualFile virtualFile, PsiFile psiFile) {
            this.filePath = filePath;
            this.virtualFile = virtualFile;
            this.psiFile = psiFile;
        }
        
        public String getFilePath() {
            return filePath;
        }
        
        public VirtualFile getVirtualFile() {
            return virtualFile;
        }
        
        public PsiFile getPsiFile() {
            return psiFile;
        }
        
        public String getFileName() {
            int lastSlash = filePath.lastIndexOf('/');
            return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            AttachedFile that = (AttachedFile) obj;
            return filePath.equals(that.filePath);
        }
        
        @Override
        public int hashCode() {
            return filePath.hashCode();
        }
        
        @Override
        public String toString() {
            return getFileName();
        }
    }
}
