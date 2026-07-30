# Java foreign functions and memory API

FFA is a more modern java api (introduced in JDK 22) for interfacing with native code, it lets you create and manipulate MemorySegments directly
and link libraries for calling. Of interest for this project was being able to get Fortran code from PROPACK to call back to a jvm function
which takes MemorySegment parameters and manipulates them as needed to implement Hankel matrix multiplication.

JNA is the older solution for this that a lot of existing libraries use.
Some other interesting things FFA opens up are tightly packed arrays of java objects via [TypedMemory](https://github.com/mamba-studio/TypedMemory),
though I'm not certain about performance for this and project Valhalla should make things easier on this end too!

## Resources

- [How to use the Foreign Function API in Java 22 to Call C Libraries](https://ifesunmola.com/how-to-use-the-foreign-function-api-in-java-22-to-call-c-libraries/)
