# chart-destroyer

A Clojure library designed to ... well, that part is up to you.

## Usage

`lein uberjar and then java -jar output 30 --dry-run`
or
```
  (environ/bind-env :HARBOR_URL "YOUR_HARBOR_URL_HERE")
  (environ/bind-env :HARBOR_TOKEN "YOUR_HARBOR_TOKEN_HERE")

export HARBOR_URL="https://your.harbor.somewhere.io"
export HARBOR_TOKEN="YOUR_HARBOR_TOKEN_HERE"
lein run --dry-run 30 
``````

## License

WTFPL