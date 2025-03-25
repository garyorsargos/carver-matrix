import React, { useEffect, useState } from "react";
import {
  Box,
  Typography,
  TextField,
  FormControl,
  FormLabel,
  FormGroup,
  FormControlLabel,
  Checkbox,
  Button,
} from "@mui/material";
import MatrixCard from "../components/containers/MatrixCard";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { ROUTES } from "../helpers/helpers";

interface CarverMatrix {
  matrixId: number;
  name: string;
  description: string;
  hosts: string[];
  participants: string[];
}

const ViewMatrix: React.FC = () => {
  const [matrices, setMatrices] = useState<CarverMatrix[]>([]);
  const [searchTerm, setSearchTerm] = useState("");
  const navigate = useNavigate();
  const [roleFilters, setRoleFilters] = useState({
    host: false,
    participant: false,
    both: false,
  });
  const [userEmail, setUserEmail] = useState<string | null>(null);

  useEffect(() => {
    const fetchUserEmail = async () => {
      try {
        const response = await axios.get("/api/user2/whoami-upsert", {
          withCredentials: true,
        });
        if (typeof response.data === "string" && response.data.includes("{")) {
          const userData = JSON.parse(response.data.split("}{")[0] + "}");
          setUserEmail(userData.email);
        }
      } catch (error) {
        console.error("Error fetching user email:", error);
      }
    };

    fetchUserEmail();
  }, []);

  // Helper to parse matrix response if it comes back as a string
  const parseMatrixResponse = (data: unknown): CarverMatrix[] => {
    if (typeof data === "string" && data.includes("[")) {
      const parts = data.split("]{");
      return JSON.parse(parts[0] + "]");
    } else if (Array.isArray(data)) {
      return data as CarverMatrix[];
    } else {
      console.error("Unexpected response format:", data);
      return [];
    }
  };

  // fetch matrices where user is host & participant
  useEffect(() => {
    if (!userEmail) return;

    const fetchAssociatedMatrices = async () => {
      try {
        // Get matrices where the user is host
        const hostUrl = `/api/carvermatrices/search?hosts=${encodeURIComponent(userEmail)}&participants=`;
        // Get matrices where the user is participant
        const participantUrl = `/api/carvermatrices/search?hosts=&participants=${encodeURIComponent(userEmail)}`;

        // Make both calls in parallel
        const [hostRes, participantRes] = await Promise.all([
          axios.get(hostUrl, { withCredentials: true }),
          axios.get(participantUrl, { withCredentials: true }),
        ]);

        const hostData = parseMatrixResponse(hostRes.data);
        const participantData = parseMatrixResponse(participantRes.data);

        // Combine the two arrays
        const combined = [...hostData, ...participantData];

        // Remove duplicates by matrixId
        const unique = combined.filter(
          (matrix, index, self) =>
            index === self.findIndex((m) => m.matrixId === matrix.matrixId)
        );

        setMatrices(unique);
      } catch (error) {
        console.error("Error fetching matrices:", error);
      }
    };

    fetchAssociatedMatrices();
  }, [userEmail]);

  const handleRoleFilterChange = (role: keyof typeof roleFilters) => {
    setRoleFilters((prev) => ({
      ...prev,
      [role]: !prev[role],
    }));
  };

  // Matrices can be filtered by name, description, or role
  const filteredMatrices = matrices.filter((matrix) => {
    // Text search filter
    const term = searchTerm.toLowerCase();
    const matchesSearch =
      matrix.name.toLowerCase().includes(term) ||
      matrix.description.toLowerCase().includes(term);

    // If no role filters are selected, we just apply text search
    if (!roleFilters.host && !roleFilters.participant && !roleFilters.both) {
      return matchesSearch;
    }

    // Role-based filtering
    if (!userEmail) return false;

    const isHost = matrix.hosts?.includes(userEmail) || false;
    const isParticipant = matrix.participants?.includes(userEmail) || false;
    const isBoth = isHost && isParticipant;

    const matchesRole =
      (roleFilters.host && isHost) ||
      (roleFilters.participant && isParticipant) ||
      (roleFilters.both && isBoth);

    return matchesSearch && matchesRole;
  });

  return (
    <Box display="flex">
      {/* Sidebar Filters */}
      <Box
        sx={{
          width: "20%",
          padding: 2,
          backgroundColor: "white",
          height: "100vh",
          boxShadow: 3,
          input: { color: "black" },
          display: "flex",
          flexDirection: "column",
          gap: 2,
        }}
      >
        <TextField
          fullWidth
          focused
          variant="outlined"
          label="Search Matrix"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
        {/* Role Filters */}
        <FormControl component="fieldset">
          <FormLabel component="legend">Role</FormLabel>
          <FormGroup>
            <FormControlLabel
              control={
                <Checkbox
                  checked={roleFilters.host}
                  onChange={() => handleRoleFilterChange("host")}
                />
              }
              label="Host"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={roleFilters.participant}
                  onChange={() => handleRoleFilterChange("participant")}
                />
              }
              label="Participant"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={roleFilters.both}
                  onChange={() => handleRoleFilterChange("both")}
                />
              }
              label="Both"
            />
          </FormGroup>
        </FormControl>

        <Button
          variant="contained"
          color="success"
          onClick={() => navigate(ROUTES.createMatrix)}
          fullWidth
          sx={{ mt: 1 }}
        >
          Create new matrix
        </Button>
      </Box>

      {/* Main Content */}
      <Box
        id="viewMatricesBox"
        sx={{
          padding: 2,
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
              onEditMatrix={() => {
                const url = `/EditMatrix?matrixId=${matrix.matrixId}`;
                navigate(url);
              }}
              titleColor="black"
              buttonVariant="contained"
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
