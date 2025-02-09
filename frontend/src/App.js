import React from "react";
import FileUploader from "./components/FileUploader";
import "./styles/App.css";

function App() {
  return (
      <div className="App">
        <h1>JPG Metadata Editor</h1>
        <FileUploader />
      </div>
  );
}

export default App;
