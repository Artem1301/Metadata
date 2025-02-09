import { useState } from "react";
import "../styles/App.css";

export default function MetadataUploader() {
    const [selectedFile, setSelectedFile] = useState(null);
    const [metadata, setMetadata] = useState("");

    const handleFileChange = (event) => {
        setSelectedFile(event.target.files[0]);
    };

    const handleMetadataChange = (event) => {
        setMetadata(event.target.value);
    };

    const handleUpload = () => {
        if (!selectedFile || !metadata) {
            alert("Please select a file and enter metadata.");
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
                <input
                    type="file"
                    accept="image/jpeg"
                    onChange={handleFileChange}
                    className="file-input"
                />
                <input
                    type="text"
                    placeholder="Enter metadata"
                    value={metadata}
                    onChange={handleMetadataChange}
                    className="text-input"
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
