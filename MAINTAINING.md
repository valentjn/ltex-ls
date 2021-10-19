<!--
   - Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
   -
   - This Source Code Form is subject to the terms of the Mozilla Public
   - License, v. 2.0. If a copy of the MPL was not distributed with this
   - file, You can obtain one at https://mozilla.org/MPL/2.0/.
   -->

# Maintainer Guidelines

## Guidelines About Issues

- **Stale issues** are closed seven days after they became stale. An issue becomes stale if:
  - The issue is missing information.
  - The issue is declared stale by a maintainer.
- **Discussions** may happen on any issue regardless of its state. Discussions don't prolong the lifetime of stale issues, and they must be about the original issue, otherwise a new issue must be opened.
- Issues may only be **reopened** if the reason for closing it doesn't exist anymore.
  - *Examples:* Missing information is provided, bug occurs again due to a regression, etc.
- Issue comments that do not contribute to the discussion (e.g., “I'm affected, too” on confirmed bug reports) may be hidden. Instead, reactions (e.g., thumbs-up) should be used to upvote issues.
- Issues that don't follow the **template** may be closed immediately.
- Issues may be **locked** if they violate the [code of conduct](https://valentjn.github.io/ltex/code-of-conduct.html).

## Guidelines About Versioning

- **Semantic versioning** is used for vscode-ltex and ltex-ls. The two versions are independent of each other.
  - For bug fixes, the patch version is increased.
  - For new features, the minor version is increased.
  - For breaking changes, the major version is increased.
    - *Explanation:* Breaking changes are changes that may require action from users (e.g., most changes of existing LT<sub>E</sub>X settings).

## Guidelines About Fundamental Changes

- **Fundamental changes** are announced as an issue, in the documentation, and/or as message boxes in VS Code three months before their implementation.
  - *Explanation:* Fundamental changes are possibly breaking changes that change the foundation of LT<sub>E</sub>X (e.g., upgrade from Java 8 to Java 11).
- **Documentation of fundamental changes** and associated deprecated settings may be deleted three months after their implementation.
