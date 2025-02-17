import React, { useEffect, useState } from "react";
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

const ViewMatrix: React.FC = () => {
  // Context and state
  // const { makeRequest } = useContext(GlobalContext);
  const [matrices, setMatrices] = useState<
    { title: string; description: string }[]
  >([]);
  const [searchTerm, setSearchTerm] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    const fetchMatrices = async () => {
      try {
        // TODO: Replace with actual API call
        const placeholderData = [
          { title: "Matrix 1", description: "Placeholder matrix 1" },
          { title: "Matrix 2", description: "Placeholder matrix 2" },
          { title: "Matrix 3", description: "Placeholder matrix 3" },
        ];
        setMatrices(placeholderData);
      } catch (error) {
        console.error("Error fetching matrices:", error);
      }
    };

    fetchMatrices();
  }, []);

  // Filter matrices based on search input
  const filteredMatrices = matrices.filter((matrix) =>
    matrix.title.toLowerCase().includes(searchTerm.toLowerCase())
  );

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
          filteredMatrices.map((matrix, index) => (
            <MatrixCard
              key={index}
              title={matrix.title}
              description={matrix.description}
              onEdit={() => navigate(`/editMatrix/${matrix.title}`)}
              onShare={() => console.log("Export", matrix.title)}
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
