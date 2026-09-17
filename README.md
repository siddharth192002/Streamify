# Streamify

Streamify is a Netflix-style video streaming backend built as four independent Spring Boot services. Videos are uploaded to Amazon S3, processed asynchronously through Apache Kafka, encoded into HLS renditions with FFmpeg, and exposed through signed streaming URLs.

## Architecture

```text
Client
	|
	+--> Content Service ----> MySQL (movie catalog)
	|
	+--> Video Service ------> S3 (original upload)
															|
															+--> Kafka: video-uploaded
																			|
																			+--> Encoding Service --> S3 (HLS output)
																																	|
																			Kafka: video-encoded <-------+
																			|
																			+--> Streaming Service --> Redis (playlist URL cache)
```

## Services

| Service           |   Port | Responsibility                                         |
| ----------------- | -----: | ------------------------------------------------------ |
| Content Service   | `8081` | Movie catalog backed by MySQL                          |
| Video Service     | `8082` | Multipart video uploads to S3                          |
| Encoding Service  | `8083` | Kafka consumer that creates HLS renditions with FFmpeg |
| Streaming Service | `8084` | Signed HLS playlist URLs with Redis caching            |

## Prerequisites

- Java 17 or later
- Maven 3.9+ (or the Maven Wrapper included in each service)
- MySQL
- Apache Kafka
- Redis
- An AWS S3 bucket and credentials with access to the bucket
- FFmpeg installed on the Encoding Service host

The default local configuration expects MySQL on `localhost:3306`, Kafka on `localhost:9092`, and Redis on `localhost:6379`. The Content Service creates `content_db` automatically when the configured MySQL user has permission to do so.

## Configuration

Set credentials and deployment-specific values through environment variables. Important variables include:

```text
DB_USERNAME
DB_PASSWORD
KAFKA_BOOTSTRAP_SERVERS
AWS_ACCESS_KEY
AWS_SECRET_KEY
AWS_REGION
AWS_BUCKET_NAME
FFMPEG_PATH
TEMP_DIR
```

Do not commit AWS credentials or other secrets. Replace any credentials that may have been exposed in local configuration before deploying this project.

## Running locally

Start MySQL, Kafka, Redis, and ensure FFmpeg is available. Then run each service in a separate terminal:

```bash
cd content-service && ./mvnw spring-boot:run
cd video-service && ./mvnw spring-boot:run
cd encoding-service && ./mvnw spring-boot:run
cd streaming-service && ./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

Run the tests for an individual service with:

```bash
./mvnw test
```

## API overview

### Content Service (`http://localhost:8081`)

| Method | Endpoint                          | Description     |
| ------ | --------------------------------- | --------------- |
| `POST` | `/api/v1/movies`                  | Add a movie     |
| `GET`  | `/api/v1/movies`                  | List all movies |
| `GET`  | `/api/v1/movies/{movieId}`        | Get a movie     |
| `GET`  | `/api/v1/movies/genre/{genre}`    | Filter by genre |
| `GET`  | `/api/v1/movies/search?title=...` | Search by title |

### Video Service (`http://localhost:8082`)

Upload a source video as multipart form data:

```bash
curl -X POST "http://localhost:8082/api/v1/videos/upload/{movieId}" \
	-F "file=@/path/to/video.mp4"
```

The upload publishes an event to Kafka and starts encoding asynchronously.

### Streaming Service (`http://localhost:8084`)

```text
GET /api/v1/stream/{movieId}
GET /api/v1/stream/{movieId}/playlist?path=...
```

The first endpoint returns the stream metadata or signed URL. The playlist endpoint serves signed HLS playlist content for the requested path.

## Health checks

Each service exposes Spring Boot Actuator health and info endpoints:

```text
GET http://localhost:<port>/actuator/health
GET http://localhost:<port>/actuator/info
```

## Project layout

```text
content-service/    Movie catalog and MySQL persistence
video-service/      Upload API and S3 integration
encoding-service/   Kafka consumer and FFmpeg encoding pipeline
streaming-service/  Redis-backed signed HLS delivery
```
