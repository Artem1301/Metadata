import { useState } from "react";
import "../styles/App.css";

export default function MetadataUploader() {
    const [selectedFiles, setSelectedFiles] = useState([]);
    const [metadataFile, setMetadataFile] = useState(null);
    const [uploadMessage, setUploadMessage] = useState("");
    const [downloadUrl, setDownloadUrl] = useState(null);

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

            if (response.ok) {
                const blob = await response.blob();
                const url = URL.createObjectURL(blob);
                setDownloadUrl(url);
                setUploadMessage("Files processed successfully! Click below to download.");
            } else {
                const text = await response.text();
                setUploadMessage("Upload error: " + text);
            }
        } catch (error) {
            setUploadMessage("Connection error with the server.");
            console.error(error);
        }
    };

    return (
        <div className="container">
            <h1>JPG Metadata Editor</h1>

            {/* File input for JPG images */}
            <input
                type="file"
                accept="image/jpeg"
                multiple
                onChange={handleFileChange}
            />

            {/* File input for metadata TXT file */}
            <input
                type="file"
                accept=".txt"
                onChange={handleMetadataFileChange}
            />

            {/* Upload button */}
            <button onClick={handleUpload}>Upload</button>

            {/* Message displaying status of upload */}
            {uploadMessage && <p>{uploadMessage}</p>}

            {/* Download link for the ZIP file containing modified images */}
            {downloadUrl && (
                <a href={downloadUrl} download="modified_images.zip">
                    Download Updated Images
                </a>
            )}

            {/* Display selected files */}
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
