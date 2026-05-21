FROM python:3.11-slim

WORKDIR /app

# Install dependencies (cache-bust: v3)
COPY backend/requirements.txt ./requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

# Copy the backend package
COPY backend/ ./backend/

# Railway injects PORT env variable at runtime
ENV PORT=8000

EXPOSE ${PORT}

# Use shell form so $PORT is expanded at runtime from Railway's env
CMD uvicorn backend.main:app --host 0.0.0.0 --port $PORT
