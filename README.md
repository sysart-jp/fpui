# Functional UI Example

## Concept

* [Functional UI \(Framework\-Free at Last\)](https://www.infoq.com/articles/functional-UI-introduction-no-framework/)
* [Elm \- A delightful language for reliable webapps](https://elm-lang.org/)
* [Why Raj](https://jew.ski/why-raj/)


## Powered by

* [Scala\.js](https://www.scala-js.org/)
* [Slinky \- Write React apps in Scala just like ES6](https://slinky.dev/)


## How to Run

以下、サブプロジェクトごとに用意されたコマンドで、アプリケーションを開発モードで起動できる（コードへの修正は hot-reloading される）。起動後、http://localhost:8080 にアクセス。

### Hello world

* [concept/src/main/scala/jp/sysart/fpui/Main.scala](concept/src/main/scala/jp/sysart/fpui/Main.scala)

```
$ sbt conceptDev
```

### 実装例: ToDo

* <http://todomvc.com/> と同じものを `FunctionalUI` で実装。
* [examples/todo/src/main/scala/jp/sysart/fpui/example/todo/Main.scala](examples/todo/src/main/scala/jp/sysart/fpui/example/todo/Main.scala)

```
$ sbt todoDev
```

### 実装例: Multipage

* アプリケーションを複数の `Page` に分解する例。
* 各ページへの routing。
* [examples/multipage/src/main/scala/jp/sysart/fpui/example/multipage/Main.scala](examples/multipage/src/main/scala/jp/sysart/fpui/example/multipage/Main.scala)

```
$ sbt multipageDev
```