FORMAT: 1A
HOST: https://heatmap.apiblueprint.org/

# Heatmap

Heatmap is a RESTful API allowing consumers to retrieve aggregate heatmap and optional generate image based on it.

## Heatmap [/v1/heatmap]

This endpoint is used to retrieve aggregated heatmap from specified period of time.

### Get a sparse Heatmap [GET]

Retrieve aggregated heatmap for specified period and id.
In case when points less than 40 percent response will be sparse.

+ Parameters
    + id: `1001` (number, required) - Id of a heatmap.
    + from: `2022-01-01T00:00:00Z` (string, required) - Timestamp for start aggregation (ISO-8601 notation).
    + until: `2022-01-02T00:00:00Z` (string, required) - Timestamp for end aggregation (ISO-8601 notation).

+ Response 200 (application/json)

    + Body

            {
                "width": 640,
                "height": 480,
                "sparse": [
                    [ 0, 0, 8.35 ],
                    [ 0, 2, 4.59 ],
                    [ 1, 0, 0.56 ],
                    [ 2, 2, 4.57 ]
                ]
            }

### Get a dense Heatmap [GET]

Retrieve aggregated heatmap for specified period and id.
In case when points greater than 40 percent response will be dense.

+ Parameters
    + id: `1001` (number, required) - id of a heatmap.
    + from: `2022-01-01T00:00:00Z` (string, required) - Timestamp for start aggregation (ISO-8601 notation).
    + until: `2022-01-02T00:00:00Z` (string, required) - Timestamp for end aggregation (ISO-8601 notation).

+ Response 200 (application/json)

    + Body

            {
                "width": 640,
                "height": 480,
                "dense": [
                    [ 8.35, 9.08, 4.59 ],
                    [ 0.56, 4.57, 8.13 ],
                    [ 0.01, 42.1, 2.43 ]
                ]
            }

### Get a Heatmap image [GET]

Retrieve aggregated heatmap for specified period and id and generate image based on it.

+ Headers
    + Content-Type: image/png

+ Parameters
    + id: `1001` (number, required) - Id of a heatmap.
    + from: `2022-01-01T00:00:00Z` (string, required) - Timestamp for start aggregation (ISO-8601 notation).
    + until: `2022-01-02T00:00:00Z` (string, required) - Timestamp for end aggregation (ISO-8601 notation).
    + opacity: `0.1` (number, optional) - Opacity of generated heatmap (available values in range 0.0 - 1.0).
        + Default: `1.0`
    + radius: `2` (number, optional) - Radius size of one point (available values in range 8 - 128).
        + Default: `64`

+ Response 200 (image/png)

![response](./response.png)

## Connection management [/v1/ping]

This endpoint is often used to test if a connection is still alive, or to measure latency.

### Ping [GET]

+ Response 200 (application/json)

        {
            "message": "pong"
        }
