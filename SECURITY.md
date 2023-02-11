<!--
   - Copyright (C) 2019-2023 Julian Valentin, LTeX Development Community
   -
   - This Source Code Form is subject to the terms of the Mozilla Public
   - License, v. 2.0. If a copy of the MPL was not distributed with this
   - file, You can obtain one at https://mozilla.org/MPL/2.0/.
   -->

# Security Policy

For the LT<sub>E</sub>X project, the security of its users is its top priority. This document contains information on which versions are supported with security updates and how to report potential security vulnerabilities.

## Supported Versions

By default, only the most recent published version of LT<sub>E</sub>X is supported with security updates.

If deemed relevant by the maintainers (depending on the severity of the security vulnerability and the need to keep older versions for compatibility), older versions of LT<sub>E</sub>X might also get security updates in exceptional circumstances.

## Definition of Vulnerabilities

LT<sub>E</sub>X follows [Microsoft's definition of security vulnerabilities](https://docs.microsoft.com/en-us/previous-versions/tn-archive/cc751383(v=technet.10)).

In this document, the term &ldquo;vulnerability&rdquo; is synonymous to &ldquo;security vulnerability.&rdquo;

## Reporting a Vulnerability

**Please do not report security vulnerabilities through public GitHub issues.**

Please report security vulnerabilities via email to valentjn (a) bsplines.org.

You can expect an initial response within 24 hours. If you do not get a response, please send a follow-up email.

In your report, please include at least the following information (as much as possible):

- Type of issue (e.g., buffer overflow, SQL injection, cross-site scripting, etc.)
- Paths of source files related to the issue
- Location of the affected source code (tag/branch/commit)
- Configuration required to reproduce the issue
- Step-by-step instructions to reproduce the issue
- Proof-of-concept or exploit code (if possible)
- Impact of the issue, including how an attacker might exploit the issue

## Process of Handling Vulnerabilities

The steps of the process of handling vulnerabilities is as follows:

1. [Initial response](#reporting-a-vulnerability)
2. Confirmation that the issue is a [security vulnerability](#definition-of-vulnerabilities)
3. All vulnerable versions have been unpublished/deleted
4. Fix is being implemented
5. Fix is implemented
6. Confirmation that the issue has been fixed in a new non-public pre-release
7. Fix is pushed and released; [responsible disclosure](#responsible-disclosure-policy)
8. [Post-mortem analysis](#post-mortem-analysis)

You will obtain an update via email as soon as the next step has been completed, but no later than 5 days after the last update.

## Responsible Disclosure Policy

LT<sub>E</sub>X follows the principle of responsible disclosure. This means that security vulnerabilities are disclosed, but only a specific period of time.

Vulnerabilities are disclosed as soon as a fix has been pushed and released or after 30 days after the initial report, whichever comes first. No information must be disclosed before the embargo ends, neither by the reporter nor by the maintainers.

Vulnerabilities are disclosed in the project

- as a new GitHub security advisory,
- as a new and pinned GitHub issue, and
- in the changelog.

In the disclosure, the reporter is given proper credit, unless they request to stay anonymous. The reporter is free to post their own analysis on personal pages.

## Post-Mortem Analysis

After the vulnerability has been disclosed, a thorough post-mortem analysis is performed to minimize the risk of future vulnerabilities of similar nature. The analysis is focused on the underlying root causes and newly implemented counter-measures.

When the post-mortem analysis is complete, the description of the GitHub issue, in which the issue has been disclosed, is amended to include the analysis.
