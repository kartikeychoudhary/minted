# Release Process — Minted

> Step-by-step guide for creating a new release of the Minted application.

---

## Prerequisites

- All bug fixes / features are merged to `main` and tested
- Both projects build cleanly: `./gradlew build` and `ng build`
- Docker Desktop is running (for image builds)
- You are authenticated to Docker Hub: `docker login`
- GitHub CLI is installed and authenticated: `gh auth status`

---

## Step 1: Bump Version Numbers

Update the version in **three** files:

| File | Field | Example |
|------|-------|---------|
| `minted-api/build.gradle` | `version = 'X.Y.Z'` | `version = '1.0.1'` |
| `minted-web/package.json` | `"version": "X.Y.Z"` | `"version": "1.0.1"` |

No version changes are needed in Docker Compose files — they use the `MINTED_VERSION` environment variable at runtime.

---

## Step 2: Build and Verify

```bash
# Backend
cd minted-api
./gradlew build

# Frontend
cd ../minted-web
npx ng build
```

Ensure both builds pass with zero errors.

---

## Step 3: Commit and Tag

```bash
cd /path/to/minted

# Stage and commit version bump + all changes
git add -A
git commit -m "release: bump versions to vX.Y.Z"

# Create an annotated tag
git tag -a vX.Y.Z -m "Release vX.Y.Z"

# Push commit and tag
git push origin main
git push origin vX.Y.Z
```

---

## Step 4: Build and Push Docker Images

Docker Hub images use the naming convention:
- `kartikey31choudhary/minted-backend:<tag>`
- `kartikey31choudhary/minted-frontend:<tag>`

Each release should push **two tags** per image: the version tag and `latest`.

```bash
cd /path/to/minted

# Build backend image
docker build -t kartikey31choudhary/minted-backend:vX.Y.Z -t kartikey31choudhary/minted-backend:latest ./minted-api

# Build frontend image
docker build -t kartikey31choudhary/minted-frontend:vX.Y.Z -t kartikey31choudhary/minted-frontend:latest -f Dockerfile .

# Push all tags
docker push kartikey31choudhary/minted-backend:vX.Y.Z
docker push kartikey31choudhary/minted-backend:latest
docker push kartikey31choudhary/minted-frontend:vX.Y.Z
docker push kartikey31choudhary/minted-frontend:latest
```

---

## Step 5: Create GitHub Release

```bash
gh release create vX.Y.Z \
  --title "Minted vX.Y.Z" \
  --notes "$(cat <<'EOF'
## What's Changed

- [List bug fixes, features, improvements]

## Docker Images

Pull the latest images:
```
docker pull kartikey31choudhary/minted-backend:vX.Y.Z
docker pull kartikey31choudhary/minted-frontend:vX.Y.Z
```

Or use docker-compose.prod.yml:
```
MINTED_VERSION=vX.Y.Z docker compose -f docker-compose.prod.yml up -d
```
EOF
)"
```

---

## Quick Reference: Version Locations

| What | Where | Notes |
|------|-------|-------|
| Backend version | `minted-api/build.gradle` → `version` | Used in JAR manifest |
| Frontend version | `minted-web/package.json` → `version` | Used in build metadata |
| Docker image tag | CLI build args (`-t ...:<tag>`) | Matches git tag |
| Git tag | `git tag -a vX.Y.Z` | Annotated tag on main |
| Compose runtime | `MINTED_VERSION` env var | Used in `docker-compose.prod.yml` |

---

## Versioning Convention

This project follows [Semantic Versioning](https://semver.org/):

- **MAJOR** (X.0.0) — Breaking changes, major redesigns
- **MINOR** (0.X.0) — New features, backward-compatible
- **PATCH** (0.0.X) — Bug fixes, small improvements

Git tags are prefixed with `v`: `v1.0.0`, `v1.0.1`, `v1.1.0`, etc.
