import React, { useState } from "react";
import axios from "axios";

const FileUploader = () => {
    const [selectedFile, setSelectedFile] = useState(null);
    const [metadata, setMetadata] = useState("");
    const [response, setResponse] = useState("");

    const handleFileChange = (event) => {
        setSelectedFile(event.target.files[0]);
    };

    const handleMetadataChange = (event) => {
        setMetadata(event.target.value);
    };

    const handleUpload = async () => {
        if (!selectedFile) {
            alert("Please select a file");
            return;
        }

        const formData = new FormData();
        formData.append("file", selectedFile);
        formData.append("metadata", metadata);

        try {
            const res = await axios.post("http://localhost:8080/api/upload", formData, {
                headers: { "Content-Type": "multipart/form-data" },
            });
            setResponse(res.data);
        } catch (error) {
            setResponse("Error uploading file");
        }
    };

    return (
        <div>
            <h2>Upload JPG with Metadata</h2>
            <input type="file" accept="image/jpeg" onChange={handleFileChange} />
            <input type="text" placeholder="Enter metadata" value={metadata} onChange={handleMetadataChange} />
            <button onClick={handleUpload}>Upload</button>
            <pre>{response}</pre>
        </div>
    );
};

export default FileUploader;
