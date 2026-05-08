# 🚀 Modernising with Java 17 — 5-Day Training Programme

**Audience:** Java 8 developers ready to move to modern Java  
**Format:** 1.5 hours × 5 days | Theory + Hands-On  
**Code base:** Every slide references real, runnable code in this repository

---

## 📐 Programme Architecture

| Day | Title | Theme | Source Modules |
|-----|-------|--------|----------------|
| [Day 1](day1-slides.md) | The Functional Revolution | Java 8 Foundation | `module1/lambdas`, `module1/streams`, `module1/optional` |
| [Day 2](day2-slides.md) | The Evolution Years | Java 9–14 Essentials | `module1/var`, `module1/datetime`, `module1/string`, `module1/httpclient`, `module1/concurrent`, `module1/collections`, `module2/switchexpressions` |
| [Day 3](day3-slides.md) | The Modern Java – Part 1 | Java 15–17 Core Features | `module2/records`, `module2/textblocks`, `module2/helpfulnpe` |
| [Day 4](day4-slides.md) | The Modern Java – Part 2 | Java 17 Power Features + Workshop | `module2/patternmatching`, `module2/sealedclasses`, `module3/` |
| [Day 5](day5-slides.md) | Bonus: The Future Is Now | Java 18–21 Preview | `bonus/virtualthreads`, `bonus/structuredconcurrency`, `bonus/recordpatterns`, `bonus/patternswitch`, `bonus/sequencedcollections` |

---

## 📦 Training Materials Index

| File | Purpose |
|------|---------|
| [day1-slides.md](day1-slides.md) | Day 1 full slide deck |
| [day2-slides.md](day2-slides.md) | Day 2 full slide deck |
| [day3-slides.md](day3-slides.md) | Day 3 full slide deck |
| [day4-slides.md](day4-slides.md) | Day 4 full slide deck |
| [day5-slides.md](day5-slides.md) | Day 5 full slide deck |
| [lab-guide.md](lab-guide.md) | Step-by-step hands-on lab instructions |
| [cheat-sheet.md](cheat-sheet.md) | One-page feature quick reference |
| [migration-checklist.md](migration-checklist.md) | 10-point Java 8 → 17 migration checklist |

---

## 🛠️ Prerequisites

| Tool | Minimum version |
|------|-----------------|
| JDK | 17 (Day 1–4), 21 (Day 5 bonus) |
| Maven | 3.6+ |
| IDE | IntelliJ IDEA 2023+ or Eclipse 2023+ recommended |

---

## 🚦 Quick Start

```bash
# Clone and build — verify all tests pass before the first session
git clone <repo-url>
cd Java_8-17
mvn test

# Day 5 bonus module (requires Java 21)
cd bonus
mvn test
```

---

## 📋 Daily Schedule Template

| Segment | Duration | Description |
|---------|----------|-------------|
| Opening Hook | 5 min | Before/after contrast to set the stage |
| Theory Blocks | 50–60 min | Concepts, JEP references, before/after code |
| Hands-On | 25–30 min | Instructor-led + individual/pair coding |
| Takeaways + Q&A | 5 min | 3-sentence summary, open floor |

---

## 🎨 Slide Design Principles

1. **Every feature slide uses 3 columns:** *Problem → Solution → Code*
2. **Before/After comparisons** are always side-by-side at the same font size
3. **No wall of text** — if a slide exceeds 5 bullets, split it
4. **Hands-on slides** show the task in a highlighted box; reveal the solution on click
5. **Running domain** — the `TradeDetailsRecord` / `TransactionService` financial domain
   appears in every day so the audience sees one codebase evolve from Java 7-style
   POJO-hell to clean, idiomatic Java 21
