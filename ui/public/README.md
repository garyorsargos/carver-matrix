### The Public Folder

The public folder is used to store static assets such as images. If you ever want to render an image in your react app, simply place the image file here in the public directory, and then use a root filepath in your code when referencing the image. Vite will resolve the absolute image path for you as a result of the development server functioning similar to a production server where your static assets are all at the root of the build directory.

##### Example

```
<img src="/some-file.svg">
```
