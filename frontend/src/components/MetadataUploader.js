import { useState, useEffect } from "react";
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import "../styles/App.css";

export default function MetadataUploader() {
    useEffect(() => {
        document.title = "Курсова Демиденко";
    }, []);

    const [selectedFiles, setSelectedFiles] = useState([]);
    const [metadataFile, setMetadataFile] = useState(null);
    const [downloadUrl, setDownloadUrl] = useState(null);
    const [metadata, setMetadata] = useState(null);
    const [loadingMetadataFor, setLoadingMetadataFor] = useState(null); // Для индикации загрузки метаданных

    const handleFileChange = (event) => {
        setSelectedFiles([...event.target.files]);
    };

    const handleMetadataFileChange = (event) => {
        setMetadataFile(event.target.files[0]);
    };

    const handleUpload = async () => {
        if (selectedFiles.length === 0 || !metadataFile) {
            toast.error("Выберите JPG и TXT файл!");
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
                toast.success("Файлы обработаны! Можно скачать.");
            } else {
                toast.error("Ошибка загрузки.");
            }
        } catch (error) {
            toast.error("Ошибка соединения с сервером.");
        }
    };

    const handleViewMetadata = async (file) => {
        setLoadingMetadataFor(file.name);
        const formData = new FormData();
        formData.append("file", file);

        try {
            const response = await fetch("http://localhost:8080/api/readMetadata", {
                method: "POST",
                body: formData
            });

            if (response.ok) {
                const data = await response.json();
                setMetadata({ fileName: file.name, data });
            } else {
                toast.error("Ошибка при чтении метаданных.");
            }
        } catch (error) {
            toast.error("Ошибка соединения.");
        } finally {
            setLoadingMetadataFor(null);
        }
    };

    const renderMetadata = (metadata) => {
        return (
            <div className="metadata-box">
                {Object.keys(metadata).map((key, index) => {
                    if (metadata[key] === null) {
                        return (
                            <div key={index}>
                                <strong>{key}:</strong> null
                            </div>
                        );
                    }
                    if (typeof metadata[key] === "object" && metadata[key] !== null) {
                        return (
                            <div key={index} className="metadata-group">
                                <strong>{key}:</strong>
                                <div className="nested-metadata">
                                    {Object.keys(metadata[key]).map((nestedKey, nestedIndex) => (
                                        <div key={nestedIndex}>
                                            <strong>{nestedKey}:</strong> {metadata[key][nestedKey] || "null"}
                                        </div>
                                    ))}
                                </div>
                            </div>
                        );
                    } else {
                        return (
                            <div key={index}>
                                <strong>{key}:</strong> {metadata[key] || "null"}
                            </div>
                        );
                    }
                })}
            </div>
        );
    };

    return (
        <div className="container">
            <h1>JPG Metadata Editor</h1>

            <input type="file" accept="image/jpeg" multiple onChange={handleFileChange} />
            <input type="file" accept=".txt" onChange={handleMetadataFileChange} />

            <button onClick={handleUpload} className="upload-button">
                Загрузить
            </button>

            {downloadUrl && (
                <div className="download-container">
                    <a href={downloadUrl} download="modified_images.zip" className="download-link">
                        Скачать файлы
                    </a>
                </div>
            )}

            {selectedFiles.length > 0 && (
                <div>
                    <h2>Выбранные файлы:</h2>
                    <ul>
                        {selectedFiles.map((file, index) => (
                            <li key={index}>
                                {file.name}
                                <button
                                    onClick={() => handleViewMetadata(file)}
                                    disabled={loadingMetadataFor === file.name}
                                    className="metadata-button"
                                >
                                    {loadingMetadataFor === file.name ? "Загрузка..." : "Показать метаданные"}
                                </button>
                            </li>
                        ))}
                    </ul>
                </div>
            )}

            {metadata && (
                <div className="metadata-container">
                    <h2>Метаданные:</h2>
                    {renderMetadata(metadata.data)}
                </div>
            )}

            <ToastContainer />
        </div>
    );
}
