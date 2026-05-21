FROM python:3.11-slim

WORKDIR /app

# Install dependencies
COPY backend/requirements.txt ./requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

# Copy the backend package
COPY backend/ ./backend/

# Railway injects PORT env variable at runtime
ENV PORT=8000

EXPOSE ${PORT}

CMD uvicorn backend.main:app --host 0.0.0.0 --port ${PORT}
