# LLM use notice

Most native bindings in this directory were initially generated using LLMs, primarily Gemini 3.1 Pro. For the sake of transparency this is an example prompt used for the wavlib bindings:

> Create java foreign function api bindings for performing a SWT using wavelib. Write helper classes in Kotlin, make the api as nice and maintainable as possible for end users, noting that we only need SWT. Below is an example of performing a 1d SWT using the library which you may use as reference, along with any other knowledge you can find on wavelib: [...]

With follow-up fixes providing an exact struct declaration from the library, some missing `reinterpret` calls for `MemorySegment`, etc...

These are currently very specific in scope to get certain things in the project working, do not treat them like proper bindings for these libraries.
