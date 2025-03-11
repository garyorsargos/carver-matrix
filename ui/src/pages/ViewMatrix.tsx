import React, { useContext, useEffect, useState } from "react";
import {
  Box,
  Typography,
  TextField,
  Button,
  FormControl,
  FormLabel,
  FormGroup,
  FormControlLabel,
  Checkbox,
} from "@mui/material";
import MatrixCard from "../components/containers/MatrixCard";
import { useNavigate } from "react-router-dom";
import { GlobalContext } from "../context/GlobalContext";
import axios from "axios";

interface CarverMatrix {
  matrixId: number;
  name: string;
  description: string;
}

const ViewMatrix: React.FC = () => {
  const [matrices, setMatrices] = useState<CarverMatrix[]>([]);
  const [searchTerm, setSearchTerm] = useState("");
  const navigate = useNavigate();
  const { userId } = useContext(GlobalContext);

  useEffect(() => {
    const fetchMatrices = async () => {
      if (!userId) {
        return;
      }
      // Assuming search endpoint can accept query parameters for hosts
      // and participants set to the logged in user's ID
      const url = "http://localhost:9002/api/carvermatrices/search";
      try {
        const response = await axios.get(url, { withCredentials: true });
        if (Array.isArray(response.data)) {
          setMatrices(response.data);
        } else {
          console.error("Unexpected response format:", response.data);
        }
      } catch (error) {
        console.error("Error fetching matrices:", error);
      }
    };
    fetchMatrices();
  }, [userId]);

  // Matrices can be filtered by name or description
  const filteredMatrices = matrices.filter((matrix) => {
    const term = searchTerm.toLowerCase();
    return (
      matrix.name.toLowerCase().includes(term) ||
      matrix.description.toLowerCase().includes(term)
    );
  });

  return (
    <Box display="flex">
      {/* Sidebar Filters */}
      <Box
        sx={{
          width: "20%",
          padding: 2,
          borderRadius: 2,
          backgroundColor: "white",
          height: "100vh",
          boxShadow: 3,
          input: { color: "black" },
        }}
      >
        <TextField
          fullWidth
          focused
          variant="outlined"
          label="Search Matrix"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          sx={{ mb: 2 }}
        />
        <Button fullWidth variant="contained" color="primary" sx={{ mb: 2 }}>
          SEARCH
        </Button>

        {/* Role Filters */}
        <FormControl component="fieldset">
          <FormLabel component="legend">Role</FormLabel>
          <FormGroup>
            <FormControlLabel control={<Checkbox />} label="Admin" />
            <FormControlLabel control={<Checkbox />} label="Observer" />
            <FormControlLabel control={<Checkbox />} label="Assigned" />
          </FormGroup>
        </FormControl>
      </Box>

      {/* Main Content */}
      <Box
        id="viewMatricesBox"
        sx={{
          backgroundColor: "#D3D3D3",
          padding: 2,
          borderRadius: 2,
          width: "80%",
          height: "100vh",
          boxShadow: 3,
          display: "flex",
          justifyContent: "flex-start",
          alignItems: "stretch",
          flexDirection: "column",
          overflow: "auto",
        }}
      >
        {filteredMatrices.length > 0 ? (
          filteredMatrices.map((matrix) => (
            <MatrixCard
              key={matrix.matrixId}
              title={matrix.name}
              description={matrix.description}
              onEdit={() => navigate(`/editMatrix/${matrix.matrixId}`)}
              onShare={() => console.log("Export", matrix.name)}
            />
          ))
        ) : (
          <Typography>No matrices found.</Typography>
        )}
      </Box>
    </Box>
  );
};

export default ViewMatrix;
