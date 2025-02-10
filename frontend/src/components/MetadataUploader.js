import { useState } from "react";
import "../styles/App.css";

export default function MetadataUploader() {
    const [selectedFiles, setSelectedFiles] = useState([]);
    const [metadataFile, setMetadataFile] = useState(null);

    const handleFileChange = (event) => {
        setSelectedFiles([...event.target.files]);
    };

    const handleMetadataFileChange = (event) => {
        setMetadataFile(event.target.files[0]);
    };

    const handleUpload = async () => {
        if (selectedFiles.length === 0 || !metadataFile) {
            alert("Please select JPG files and a TXT file with metadata.");
            return;
        }

        const formData = new FormData();
        selectedFiles.forEach(file => formData.append("files", file));
        formData.append("metadata", metadataFile);

        try {
            const response = await fetch("http://localhost:8080/api/upload", {
                method: "POST",
                body: formData
            });

            const text = await response.text();
            console.log(text);

            if (response.ok) {
                alert("Files uploaded successfully!");
            } else {
                alert("Upload error: " + text);
            }
        } catch (error) {
            alert("Connection error with the server.");
            console.error(error);
        }
    };

    return (
        <div className="container">
            <h1>JPG Metadata Editor</h1>
            <input type="file" accept="image/jpeg" multiple onChange={handleFileChange} />
            <input type="file" accept=".txt" onChange={handleMetadataFileChange} />
            <button onClick={handleUpload}>Upload</button>

            {selectedFiles.length > 0 && (
                <div>
                    <h2>Selected files:</h2>
                    <ul>
                        {selectedFiles.map((file, index) => (
                            <li key={index}>{file.name}</li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    );
}
