# Git提交日志生成与推送技能

**用途：** 根据git变动自动生成符合项目规范的提交日志，处理push conflicts和secrets扫描，最后推送到远端。

**适用场景：** 完成一个功能或bug修复后，需要快速提交并推送代码到远端仓库。

---

## 工作流步骤

### 第一阶段：分析和规划

1. **检查git状态**
   ```bash
   git status --short
   ```
   了解有哪些staged、unstaged、untracked的文件

2. **查看最近提交规范**
   ```bash
   git log --oneline -5
   ```
   确认项目使用的commit message格式（本项目使用 `type(scope): 中文描述`）

3. **分析改动范围**
   - 统计改动的文件数量和类型
   - 识别主要改动的功能块（backend/frontend/docs/config等）
   - 评估是否需要多行提交信息

4. **确定commit type**
   常见类型：
   - `feat`: 新功能
   - `fix`: bug修复
   - `refactor`: 代码重构
   - `docs`: 文档更新
   - `chore`: 配置/工具更新
   - `style`: 代码格式（不影响逻辑）

5. **生成提交信息模板**
   ```
   <type>(<scope>): <中文简述>
   
   - 详细改动点1
   - 详细改动点2
   - ...
   ```

### 第二阶段：提交和推送

6. **暂存所有改动**
   ```bash
   git add -A
   ```
   （如需谨慎提交，可先用 `git add -p` 逐块审视）

7. **创建提交**
   - **简单方式**（单行消息）：
     ```bash
     git commit -m "type(scope): 提交信息"
     ```
   
   - **完整方式**（多行消息，避免PowerShell转义问题）：
     ```bash
     @"
     feat(backend): 主要改动描述
     
     - 详细改动1
     - 详细改动2
     "@ | Out-File -Encoding utf8 commit_msg.txt
     git commit -F commit_msg.txt
     ```

8. **处理推送冲突**
   如果远端已有新提交：
   ```bash
   git pull origin <branch> --rebase
   ```
   这样会将本地提交rebase到远端最新版本之上

9. **推送到远端**
   ```bash
   git push origin <branch>
   ```

### 第三阶段：处理push protection

10. **识别secrets扫描失败**
    - GitHub会拒绝包含secrets的push
    - 错误信息会指出具体文件和行号
    - 常见secrets：API密钥、OAuth token、密码等

11. **移除secrets**
    - 找到包含secrets的文件
    - 将真实密钥替换为占位符（如 `YOUR_API_KEY`）
    - 如果是配置文件，应该提交 `.example` 版本而非包含真实密钥的版本

12. **修正并强制推送**
    ```bash
    git add -A
    git commit --amend --no-edit   # 修改前一个提交
    git push origin <branch> -f    # 强制推送（谨慎使用）
    ```

13. **清理临时文件**
    ```bash
    rm commit_msg.txt
    ```

### 第四阶段：验证

14. **确认推送成功**
    ```bash
    git log --oneline -3
    ```
    确保 `HEAD` 和 `origin/<branch>` 都指向相同的提交

---

## 项目特定规范

### WeChat-Mini-Program 项目

**Commit规范：**
- 格式：`type(scope): 中文描述`
- 常用scope：`frontend`, `backend`, `docs`, `config`, `chore`
- 使用中文简述和详细的改动清单

**示例：**
```
feat(backend): 初始化Spring Boot后端服务完整架构

- 新增wechat-elderly-service (Spring Boot 3 + MyBatis-Plus + MySQL)
- 实现用户认证 (JWT + 拦截器)
- 实现订单管理系统
- 集成AI问答模块 (百度/腾讯混元)
- 完整数据库schema初始化脚本
```

**Secrets检查列表：**
- ❌ 真实的API密钥
- ❌ 数据库真实密码
- ❌ OAuth tokens
- ✅ 占位符如 `YOUR_API_KEY`, `YOUR_PASSWORD` 

**敏感文件处理：**
- 不提交 `.env` 文件（包含真实密钥）
- 提交 `.env.example` 版本（占位符）
- 使用 `.gitignore` 排除敏感配置文件

---

## 常见问题

### Q: 如何处理已提交的secrets？
A: 
1. 使用 `git filter-branch` 或 `git-filter-repo` 从历史中删除
2. 重置远端的secrets（如API密钥）
3. 强制推送更新后的历史

### Q: 提交后发现遗漏了文件怎么办？
A:
```bash
git add <forgotten-file>
git commit --amend --no-edit
git push origin <branch> -f
```

### Q: 如何编辑已推送的提交信息？
A:
```bash
# 修改最后一个提交
git commit --amend -m "新的提交信息"
git push origin <branch> -f

# 修改历史提交（谨慎使用）
git rebase -i <commit-hash>^
# 编辑后：
git push origin <branch> -f
```

### Q: 为什么推送被rejected？
A: 常见原因：
1. 远端有新提交 → `git pull --rebase` 后重新push
2. 包含secrets → 移除后 amend 并强制推送
3. 分支保护规则 → 检查仓库设置或联系管理员

---

## 提示和最佳实践

✅ **做这些：**
- 每个逻辑上的改动作为一次提交
- 提交信息简洁但描述清楚
- 定期pull最新的远端改动以减少冲突
- 在push前检查 `git log -1` 确认内容正确
- 使用 `git status` 确保所有改动都已staged

❌ **避免这些：**
- 不要直接push到main/master（除非必须）
- 不要提交secrets或敏感信息
- 不要频繁强制推送到shared分支
- 不要在修改历史后忘记更新PR描述
- 不要把多个无关的改动混在一个提交里

---

## 快速命令参考

```bash
# 完整工作流（适合大改动）
git status --short
git log --oneline -5
git add -A
git commit -m "type(scope): 提交信息"
git pull origin develop --rebase
git push origin develop

# 修复secrets并重新推送
git add -A
git commit --amend --no-edit
git push origin develop -f

# 验证推送成功
git log --oneline -3
```

---

**版本：** 1.0  
**最后更新：** 2026-05-05  
**适用项目：** WeChat-Mini-Program (Spring Boot + Vue 3 + Uni-app)
