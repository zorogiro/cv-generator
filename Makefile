# cv-generator — project tasks
#
# Run from Git Bash on Windows (make.exe from GnuWin32 is on PATH).
# Windows Command Prompt users: replace mvnw.cmd with mvnw.cmd in MVNW below.
#
# AI features: export ANTHROPIC_API_KEY=sk-ant-... before running the backend.
# Without the key the backend starts in stub mode (AI endpoints return a notice).

MAKE     := make
MVNW     := mvnw.cmd
FRONTEND := frontend

.DEFAULT_GOAL := help

.PHONY: help \
        dev backend-run frontend-serve \
        build backend-build frontend-build frontend-build-prod \
        install frontend-install \
        test backend-test \
        clean backend-clean

# ── Help ──────────────────────────────────────────────────────────────────────
help:
	@echo ""
	@echo "cv-generator — available make targets"
	@echo ""
	@echo "  Development"
	@echo "    make dev                  Start backend :8080 + Angular dev server :4200 in parallel"
	@echo "    make backend-run          mvnw.cmd spring-boot:run"
	@echo "    make frontend-serve       ng serve  (proxies /api → :8080)"
	@echo ""
	@echo "  Build"
	@echo "    make build                Build backend JAR + Angular bundle"
	@echo "    make backend-build        mvnw.cmd clean package -DskipTests"
	@echo "    make frontend-build       ng build (development)"
	@echo "    make frontend-build-prod  ng build --configuration production"
	@echo ""
	@echo "  Test"
	@echo "    make test                 mvnw.cmd test"
	@echo ""
	@echo "  Setup"
	@echo "    make install              npm install in frontend/"
	@echo ""
	@echo "  Clean"
	@echo "    make clean                mvn clean + rm frontend/dist"
	@echo ""
	@echo "  Quick links when running:"
	@echo "    API          http://localhost:8080/api/resumes"
	@echo "    H2 console   http://localhost:8080/h2-console  (JDBC: jdbc:h2:mem:cvdb  user: sa  pw: <blank>)"
	@echo "    Angular app  http://localhost:4200"
	@echo ""
	@echo "  AI (Claude via Anthropic):"
	@echo "    export ANTHROPIC_API_KEY=sk-ant-...   # then make backend-run"
	@echo "    No key = stub mode; AI endpoints return '[AI unavailable]' prefix"
	@echo ""

# ── Development ───────────────────────────────────────────────────────────────
# -j2 runs both targets in parallel; Ctrl+C stops both.
dev:
	$(MAKE) -j2 backend-run frontend-serve

backend-run:
	$(MVNW) spring-boot:run

frontend-serve:
	cd $(FRONTEND) && ng serve

# ── Build ─────────────────────────────────────────────────────────────────────
build: backend-build frontend-build

backend-build:
	$(MVNW) clean package -DskipTests

frontend-build:
	cd $(FRONTEND) && ng build

frontend-build-prod:
	cd $(FRONTEND) && ng build --configuration production

# ── Setup ─────────────────────────────────────────────────────────────────────
install:
	cd $(FRONTEND) && npm install

frontend-install: install

# ── Test ──────────────────────────────────────────────────────────────────────
test:
	$(MVNW) test

backend-test: test

# ── Clean ─────────────────────────────────────────────────────────────────────
clean: backend-clean
	rm -rf $(FRONTEND)/dist

backend-clean:
	$(MVNW) clean
