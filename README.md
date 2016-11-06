Nash
===

This is a JSON object migration tool using Java 8's Nashorn engine to
apply migration scripts written in Javascript to JSON objects.

An Example:
---

Create an instance of `Nash` with the directory containing migrations written in Javascript.
 
```scala
val nash = new Nash(new File("migrations"))
```

Assume some JSON that looks like this:

```js
{"version": 1, "name":"channing"}
{"version": 1, "name":"lance"}
```

and in the migrations directory there is a file `001_bump_version`

```js
var migrate = function(json) {
  json.version=2
  return json;
};
```

Running `nash(objects)` will yield

```js
{"version":2,"name":"channing"}
{"version":2,"name":"lance"}
```