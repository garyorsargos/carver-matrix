import {
  Box,
  Button,
  FormControl,
  FormControlLabel,
  FormLabel,
  MenuItem,
  Paper,
  Select,
  SelectChangeEvent,
  Switch,
  Typography,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  TextField,
  Snackbar,
  Alert,
} from "@mui/material";
import { useState } from "react";
import { IconButton } from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import { whoamiUpsert, createMatrix } from "./apiService";
import SendIcon from "@mui/icons-material/Send";
import { useNavigate } from "react-router-dom";

export const CreateMatrix: React.FC = () => {
  const [RoleBasedChecked, setRoleBasedChecked] = useState(true);
  const [value, setValue] = useState<number>(5);
  const [randomAssigned, setRandomAssigned] = useState<string>("random");
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const navigate = useNavigate();
  const [targets, setTargets] = useState<string[]>([]);
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarErrorOpen, setSnackbarErrorOpen] = useState(false);
  const [titleError, setTitleError] = useState(false);
  const [participantsData, setParticipantsData] = useState<
    { email: string; role: string }[]
  >([]);

  const initialMultipliers = {
    Criticality: 1.0,
    Vulnerability: 1.0,
    Accessibility: 1.0,
    Effect: 1.0,
    Recoverability: 1.0,
    Recognizability: 1.0,
  };

  const [multipliers, setMultipliers] = useState(initialMultipliers);

  const carverOrder = [
    { key: "Criticality", header: "C" },
    { key: "Accessibility", header: "A" },
    { key: "Recoverability", header: "R" },
    { key: "Vulnerability", header: "V" },
    { key: "Effect", header: "E" },
    { key: "Recognizability", header: "R" },
  ];

  // Participant management
  const handleAddParticipant = () => {
    setParticipantsData([
      ...participantsData,
      { email: "", role: "Participant" },
    ]);
  };

  const handleParticipantEmailChange = (
    index: number,
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const newParticipants = [...participantsData];
    newParticipants[index].email = event.target.value;
    setParticipantsData(newParticipants);
  };

  const handleParticipantRoleChange = (
    index: number,
    event: SelectChangeEvent<string>
  ) => {
    const newParticipants = [...participantsData];
    newParticipants[index].role = event.target.value;
    setParticipantsData(newParticipants);
  };

  const handleDeleteParticipant = (index: number) => {
    setParticipantsData(participantsData.filter((_, i) => i !== index));
  };

  // Target management
  const handleAddTarget = () => {
    setTargets([...targets, ""]);
  };

  const handleTargetChange = (
    index: number,
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const newTargets = [...targets];
    newTargets[index] = event.target.value;
    setTargets(newTargets);
  };

  const handleDeleteTarget = (index: number) => {
    setTargets(targets.filter((_, i) => i !== index));
  };

  // Dropdown options for multipliers
  const options = [
    0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9,
    2.0,
  ];

  const multipliersHandleChange =
    (label: string) => (event: SelectChangeEvent<unknown>) => {
      const val = event.target.value as number;
      setMultipliers((prev) => ({
        ...prev,
        [label]: val,
      }));
    };

  const roleBasedHandleToggle = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setRoleBasedChecked(event.target.checked);
  };

  const scoreRangeHandleChange = (event: SelectChangeEvent<number>) => {
    setValue(event.target.value as number);
  };

  const dataEntryHandleChange = (event: SelectChangeEvent<string>) => {
    setRandomAssigned(event.target.value as string);
  };

  const handleCreateMatrix = async () => {
    try {
      // Duplicate check for targets
      const trimmedTargets = targets.map((target) => target.trim());
      const uniqueTargets = new Set(trimmedTargets);
      if (uniqueTargets.size !== trimmedTargets.length) {
        setSnackbarErrorOpen(true);
        return;
      }

      if (!title.trim()) {
        setTitleError(true);
        return;
      } else {
        setTitleError(false);
      }

      const whoamiUpsertResponse = await whoamiUpsert();
      const rawData = whoamiUpsertResponse.data;
      const firstObjectEnd = rawData.indexOf("}") + 1;
      const firstObjectStr = rawData.substring(0, firstObjectEnd);
      const parsedFirstObject = JSON.parse(firstObjectStr);
      const { userId, email } = parsedFirstObject;

      const items = targets.map((target) => ({
        itemName: target,
        criticality: 0,
        accessibility: 0,
        recoverability: 0,
        vulnerability: 0,
        effect: 0,
        recognizability: 0,
      }));

      const hosts = [
        email,
        ...participantsData
          .map((p) =>
            p.role === "Host" || p.role === "Host and Participant" ? p.email : null
          )
          .filter((email): email is string => email !== null),
      ];

      const participantEmails = participantsData
        .map((p) =>
          p.role === "Participant" || p.role === "Host and Participant" ? p.email : null
        )
        .filter((email): email is string => email !== null);

      const matrixData = {
        name: title,
        description: description,
        hosts: hosts,
        participants: participantEmails,
        cMulti: multipliers["Criticality"],
        aMulti: multipliers["Accessibility"],
        rMulti: multipliers["Recoverability"],
        vMulti: multipliers["Vulnerability"],
        eMulti: multipliers["Effect"],
        r2Multi: multipliers["Recognizability"],
        randomAssignment: randomAssigned === "random" ? true : false,
        roleBased: RoleBasedChecked,
        fivePointScoring: value === 5 ? true : false,
        items: items,
      };

      const response = await createMatrix(matrixData, userId);
      console.log("Matrix Created:", response.data);
      setSnackbarOpen(true);
      setTimeout(() => {
        navigate("/ViewMatrix");
      }, 3000);
    } catch (error) {
      console.error("Error creating matrix or verifying user", error);
    }
  };

  return (
    <Box
      sx={{
        backgroundColor: "lightgray",
        height: "100vh",
        width: "100vw",
        display: "flex",
        justifyContent: "space-around",
        alignItems: "flex-start",
        pt: 1,
      }}
    >
      {/* Left section: Matrix parameters and multipliers */}
      <Box
        id="matrixCreatorBox"
        sx={{
          backgroundColor: "white",
          p: 2,
          borderRadius: 2,
          width: "48%",
          height: "85%",
          boxShadow: 3,
          display: "flex",
          flexDirection: "column",
          overflow: "auto",
        }}
      >
        <Box display="flex" alignItems="center">
          <TextField
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            variant="standard"
            placeholder="Enter matrix Title..."
            InputProps={{
              disableUnderline: false,
              style: { fontSize: "3rem", color: "black" },
            }}
            sx={{ flexGrow: 1 }}
            error={titleError}
            helperText={titleError ? "Matrix title is required" : ""}
          />
          <Button
            variant="contained"
            onClick={handleCreateMatrix}
            endIcon={<SendIcon />}
            color="success"
            sx={{ ml: 20, borderRadius: "20px", width: "170px" }}
          >
            Create Matrix
          </Button>
        </Box>

        <Snackbar
          open={snackbarOpen}
          autoHideDuration={3000}
          onClose={() => setSnackbarOpen(false)}
        >
          <Alert
            onClose={() => setSnackbarOpen(false)}
            severity="success"
            variant="filled"
            sx={{ width: "100%" }}
          >
            Matrix Created Succesfully! Redirecting to 'View Matrix' page
          </Alert>
        </Snackbar>

        <Snackbar
          open={snackbarErrorOpen}
          autoHideDuration={3000}
          onClose={() => setSnackbarErrorOpen(false)}
        >
          <Alert
            onClose={() => setSnackbarErrorOpen(false)}
            severity="error"
            variant="filled"
            sx={{ width: "100%" }}
          >
            You cannot have duplicate target names
          </Alert>
        </Snackbar>

        <TextField
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          multiline
          rows={1}
          variant="outlined"
          fullWidth
          placeholder="Enter matrix description..."
          InputProps={{
            disableUnderline: true,
            style: { fontSize: "1rem", color: "black" },
          }}
          sx={{ flexGrow: 1 }}
        />

        <Box
          id="matrixParametersBox"
          sx={{
            backgroundColor: "white",
            width: "100%",
            mt: 3,
            display: "flex",
            flexDirection: "column",
          }}
        >
          <Typography variant="h4">Matrix Parameters</Typography>
          <Box
            sx={{
              display: "flex",
              flexDirection: "row",
              gap: 10,
              mt: 2,
            }}
          >
            <Box sx={{ flex: 1, display: "flex", flexDirection: "column" }}>
              <FormControl fullWidth>
                <FormLabel id="score-range-label">Score Range</FormLabel>
                <Select
                  labelId="score-range-label"
                  value={value}
                  onChange={scoreRangeHandleChange}
                  sx={{
                    "& .MuiInputBase-input": { color: "black" },
                    "& .MuiSelect-icon": { color: "black" },
                    border: "1px solid lightgray",
                    borderRadius: "20px",
                    mb: 1,
                  }}
                >
                  <MenuItem value={5}>5-Point Scoring</MenuItem>
                  <MenuItem value={10}>10-Point Scoring</MenuItem>
                </Select>
              </FormControl>
              <Typography>Role-Based Matrix</Typography>
              <FormControlLabel
                control={
                  <Switch
                    checked={RoleBasedChecked}
                    onChange={roleBasedHandleToggle}
                    sx={{
                      "& .MuiSwitch-track": {
                        borderRadius: "20px",
                        backgroundColor: RoleBasedChecked ? "blue" : "red",
                        opacity: 1,
                      },
                    }}
                  />
                }
                label={RoleBasedChecked ? "Enabled" : "Disabled"}
                sx={{ mb: 1 }}
              />
            </Box>
            <Box sx={{ flex: 1, display: "flex", flexDirection: "column" }}>
              <FormControl fullWidth>
                <FormLabel id="data-entry-label">Data Entry Assignment Method</FormLabel>
                <Select
                  labelId="data-entry-label"
                  value={randomAssigned}
                  onChange={dataEntryHandleChange}
                  sx={{
                    "& .MuiInputBase-input": { color: "black" },
                    "& .MuiSelect-icon": { color: "black" },
                    border: "1px solid lightgray",
                    borderRadius: "20px",
                    mb: 1,
                  }}
                >
                  <MenuItem value={"random"}>Random</MenuItem>
                  <MenuItem value={"assigned"}>Assigned</MenuItem>
                </Select>
              </FormControl>
            </Box>
          </Box>
          <Box
            id="GlobalCategoryMultipliersBox"
            sx={{
              backgroundColor: "white",
              width: "80%",
              mt: 2,
              display: "flex",
              flexDirection: "column",
            }}
          >
            <Typography variant="h4">Global Category Multipliers</Typography>
            <Table>
              <TableHead>
                <TableRow>
                  {carverOrder.map((item) => (
                    <TableCell
                      key={item.key}
                      align="center"
                      sx={{ color: "black", fontWeight: "bold" }}
                    >
                      {item.header}
                    </TableCell>
                  ))}
                </TableRow>
              </TableHead>
              <TableBody>
                <TableRow>
                  {carverOrder.map((item) => (
                    <TableCell key={item.key} align="center">
                      <Select
                        value={
                          multipliers[item.key as keyof typeof initialMultipliers]
                        }
                        onChange={multipliersHandleChange(item.key)}
                        size="small"
                        sx={{
                          "& .MuiInputBase-input": { color: "black" },
                          "& .MuiSelect-icon": { color: "black" },
                          border: "1px solid lightgray",
                          borderRadius: "20px",
                        }}
                      >
                        {options.map((option) => (
                          <MenuItem key={option} value={option}>
                            {option.toFixed(1)}x
                          </MenuItem>
                        ))}
                      </Select>
                    </TableCell>
                  ))}
                </TableRow>
              </TableBody>
            </Table>
          </Box>
        </Box>
      </Box>

      {/* Combined Targets & Roles in one box (side-by-side) */}
      <Box
        id="targetsAndRolesBox"
        sx={{
          backgroundColor: "white",
          p: 2,
          borderRadius: 2,
          width: "45%",
          height: "85%",
          boxShadow: 3,
          display: "flex",
          flexDirection: "row",
          gap: 2,
          overflow: "auto",
        }}
      >
        {/* Targets Column */}
        <Box
          sx={{
            flex: 1,
            display: "flex",
            flexDirection: "column",
          }}
        >
          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              mb: 2,
            }}
          >
            <Typography variant="h4">Targets</Typography>
            <Button
              variant="contained"
              sx={{ borderRadius: "20px", width: "100px" }}
              onClick={handleAddTarget}
            >
              Add
            </Button>
          </Box>
          {targets.map((target, index) => (
            <Box
              key={index}
              sx={{
                display: "flex",
                alignItems: "center",
                gap: 1,
                mb: 1,
                height: "56px",
              }}
            >
              <TextField
                value={target}
                onChange={(e) => handleTargetChange(index, e)}
                variant="outlined"
                fullWidth
                placeholder="Enter target..."
                sx={{
                  border: "1px solid #ccc",
                  borderRadius: "20px",
                  input: { color: "black" },
                  height: "100%",
                }}
              />
              <IconButton onClick={() => handleDeleteTarget(index)}>
                <DeleteIcon color="error" />
              </IconButton>
            </Box>
          ))}
        </Box>

        {/* Roles Column */}
        <Box
          sx={{
            flex: 1.25,
            display: "flex",
            flexDirection: "column",
          }}
        >
          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              mb: 2,
            }}
          >
            <Typography variant="h4">Roles</Typography>
            <Button
              variant="contained"
              sx={{ borderRadius: "20px", width: "100px" }}
              onClick={handleAddParticipant}
            >
              Invite
            </Button>
          </Box>
          {participantsData.map((participant, index) => (
            <Paper
              key={index}
              elevation={0}
              sx={{
                color: "black",
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                gap: 1,
                border: "1px solid #ccc",
                borderRadius: "20px",
                backgroundColor: "white",
                mb: 1,
                pl: 1,
                height: "56px",
              }}
            >
              <TextField
                value={participant.email}
                onChange={(e) => handleParticipantEmailChange(index, e)}
                variant="standard"
                placeholder="Enter email..."
                InputProps={{
                  disableUnderline: false,
                  style: { fontSize: "1rem", color: "black" },
                }}
                sx={{ flexGrow: 1 }}
              />
              <Select
                value={participant.role}
                onChange={(e) => handleParticipantRoleChange(index, e)}
                sx={{
                  color: "gray",
                  backgroundColor: "white",
                  height: "40px",
                }}
              >
                <MenuItem value="Participant">Participant</MenuItem>
                <MenuItem value="Host">Host</MenuItem>
                <MenuItem value="Host and Participant">Host and Participant</MenuItem>
              </Select>
              <IconButton onClick={() => handleDeleteParticipant(index)}>
                <DeleteIcon color="error" />
              </IconButton>
            </Paper>
          ))}
        </Box>
      </Box>
    </Box>
  );
};

export default CreateMatrix;

