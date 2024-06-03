# Contribution Guidelines

Please read these guidelines before contributing to the project.

## Git Workflow

There will be 2 main branches for this project:

1. `master` - The branch for the official ready release.
2. `dev` - The current development version.

Before a project member starts implementing their own idea, please follow the guidelines below:

1. Pull the latest version of the `dev` branch.

```bash
git checkout dev
git pull
```

1. Create a feature branch from the current `dev` branch. Feature branches should always be named `playground-<name>`. For example, `playground-johndoe`.

```bash
git checkout -b playground-johndoe
git push --set-upstream origin playground-johndoe
```

1. Start coding in your own branch. Commit all the changes into your own branch.
