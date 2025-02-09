import { useState } from "react";
import "../styles/App.css";

export default function MetadataUploader() {
    const [selectedFile, setSelectedFile] = useState(null);
    const [metadataFile, setMetadataFile] = useState(null);
    const [metadata, setMetadata] = useState("");

    const handleFileChange = (event) => {
        setSelectedFile(event.target.files[0]);
    };

    const handleMetadataFileChange = (event) => {
        const file = event.target.files[0];
        setMetadataFile(file);
        if (file) {
            const reader = new FileReader();
            reader.onload = (e) => setMetadata(e.target.result);
            reader.readAsText(file);
        }
    };

    const handleUpload = () => {
        if (!selectedFile || !metadata) {
            alert("Please select a JPG file and a metadata file.");
            return;
        }

        const formData = new FormData();
        formData.append("file", selectedFile);
        formData.append("metadata", metadata);

        console.log("File and metadata ready for upload", formData);
        alert("File uploaded successfully!");
    };

    return (
        <div className="container">
            <h1 className="title">JPG Metadata Editor</h1>
            <div className="upload-box">
                <h2 className="subtitle">Upload JPG with Metadata</h2>
                <label className="file-label">Select JPG File:</label>
                <input
                    type="file"
                    accept="image/jpeg"
                    onChange={handleFileChange}
                    className="file-input"
                />
                <label className="file-label">Select Metadata File (TXT):</label>
                <input
                    type="file"
                    accept=".txt"
                    onChange={handleMetadataFileChange}
                    className="file-input"
                />
                <button
                    onClick={handleUpload}
                    className="upload-button"
                >
                    Upload
                </button>
            </div>
        </div>
    );
}