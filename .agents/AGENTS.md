# Workspace Customization Rules

- **Strict File Modification Rule**: Do not delete existing files or create new files to replace them. All modifications must be made directly within the existing files containing `TODO(student)`.
- **Permission for structural changes**: If any file deletion or new file creation is deemed necessary, the agent must ask the user for permission before executing.
- **Explanation Rule**: Before making any file modifications, the agent must write a complete explanation of what was written, what the components are, how they work, and why they were designed that way, to ensure the user fully understands and can approve the changes.
- **Strict Layering Rule**: The backend code must strictly follow the defined architecture layering. Controllers should only interact with application services, application services should only interact with domain services, and domain services should only interact with repositories. No cross-layer direct calls are allowed (e.g., a controller should not call a repository directly).
