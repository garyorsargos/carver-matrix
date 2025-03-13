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
import SendIcon from '@mui/icons-material/Send';
import { useNavigate } from "react-router-dom";

export const CreateMatrix: React.FC = () => {
  const [RoleBasedChecked, setRoleBasedChecked] = useState(true);
  const [value, setValue] = useState<number>(5);
  const [randomAssigned, setRandomAssigned] = useState<string>('random');
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const navigate = useNavigate();
  const [targets, setTargets] = useState<string[]>([]);
  const [snackbarOpen, setSnackbarOpen] = useState(false);
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

  // Add a new participant row
const handleAddParticipant = () => {
  setParticipantsData([
    ...participantsData,
    { email: "", role: "Participant" }, // default role is "Participant"
  ]);
};

// Update email for a specific participant
const handleParticipantEmailChange = (
  index: number,
  event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
) => {
  const newParticipants = [...participantsData];
  newParticipants[index].email = event.target.value;
  setParticipantsData(newParticipants);
};

// Update role for a specific participant
const handleParticipantRoleChange = (
  index: number,
  event: SelectChangeEvent<string>
) => {
  const newParticipants = [...participantsData];
  newParticipants[index].role = event.target.value;
  setParticipantsData(newParticipants);
};

// Delete a participant row
const handleDeleteParticipant = (index: number) => {
  setParticipantsData(participantsData.filter((_, i) => i !== index));
};  

  const handleAddTarget = () => {
    setTargets([...targets, ""]);
  };

  const handleTargetChange = (
    index: number,
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
  ) => {
    const newTargets = [...targets];
    newTargets[index] = event.target.value;
    setTargets(newTargets);
  };

  const handleDeleteTarget = (index: number) => {
    setTargets(targets.filter((_, i) => i !== index));
  };

  // Options for the dropdown for Global Category Multipliers
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
    event: React.ChangeEvent<HTMLInputElement>,
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
    try
    {
      if (!title.trim()) 
      {
        setTitleError(true);
        return;
      } 
      else 
      {
        setTitleError(false);
      }
    
      const whoamiUpsertResponse = await whoamiUpsert();
      const rawData = whoamiUpsertResponse.data; // This is a string containing two JSON objects, the first we only care about.

      // Find the end of the first JSON object by locating the first closing brace
      const firstObjectEnd = rawData.indexOf('}') + 1;
      
      // Isolate the first JSON object using substring
      const firstObjectStr = rawData.substring(0, firstObjectEnd);

      // Parse said first object for necessary fields (i.e., userId)
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
        .filter((email): email is string => email !== null)
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
        randomAssignment: randomAssigned === 'random' ? true : false,
        roleBased: RoleBasedChecked,
        fivePointScoring: value,
        items: items,
      }
      const response = await createMatrix(matrixData, userId);
      console.log("Matrix Created:", response.data);
      setSnackbarOpen(true);
      setTimeout(() => {
        navigate("/ViewMatrix");
      }, 3000);
    } 
    catch (error) 
    {
      console.error('Error creating matrix or verifying user', error);
    }
  };

  return (
    // This Box is for the whole screen (background); where every other box will be built on.
    <Box
      sx={{
        backgroundColor: "lightgray",
        height: "100vh",
        width: "100vw",
        display: "flex",
        justifyContent: "left",
        alignItems: "flex-start",
        p: 2,
      }}
    >
      {/* This Box is the Left-Half of the screen and it deals with filling the Matrix Parameters and Global Category Multipliers*/}
      <Box
        id="matrixCreatorBox"
        sx={{
          backgroundColor: "white",
          p: 2,
          borderRadius: 2,
          width: "48%",
          height: "85%",
          boxShadow: 3,
          position: "center",
          top: "10px",
          display: "flex",
          alignItems: "flex-start",
          flexDirection: "column",
          overflow: "auto",
        }}
      >
        {/* This Box is for the Title and Save Button */}
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
          {/* The Save button which sends the create matrix request to backend */}
          <Button
            variant="contained"
            onClick={handleCreateMatrix}
            endIcon={<SendIcon />}
            color="success"
            sx={{ ml: 20, borderRadius: "20px", width: "170" }}
          >
            Create Matrix
          </Button>
        </Box>

        <Snackbar open={snackbarOpen} autoHideDuration={3000} onClose={() => setSnackbarOpen(false)}>
          <Alert
            onClose={() => setSnackbarOpen(false)}
            severity="success"
            variant="filled"  
            sx={{ width: '100%' }}
          >
            Matrix Created Succesfully! Redirecting to 'View Matrix' page
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

        {/* This Box is for the Matrix Parameters component */}
        <Box
          id="matrixParametersBox"
          sx={{
            backgroundColor: "white",
            width: "100%",
            height: "100%",
            position: "center",
            display: "flex",
            alignItems: "flex-start",
            flexDirection: "column",
            marginBottom: "0px",
            marginTop: "30px",
          }}
        >
          <Typography variant="h4">Matrix Parameters</Typography>
          {/* Flex container to hold two columns side by side */}
          <Box
            sx={{
              display: "flex",
              flexDirection: "row",
              alignItems: "flex-start",
              gap: 10,
              mt: 2,
            }}
          >
            {/* Left Column: Score Range & Role Based Matrix */}
            <Box sx={{ flex: 1, display: "flex", flexDirection: "column" }}>
              {/* Score Range Label */}
              <FormControl sx={{ width: "100%" }}>
                <FormLabel id="score-range-label">Score Range</FormLabel>
                <Select
                  labelId="score-range-label"
                  id="score-range-select"
                  value={value}
                  onChange={scoreRangeHandleChange}
                  label="Score Range"
                  sx={{
                    "& .MuiInputBase-input": {
                      color: "black", // Text color inside the Select box
                    },
                    "& .MuiSelect-icon": {
                      color: "black",
                    },
                    border: "1px solid lightgray",
                    borderRadius: "20px",
                    marginBottom: "10px",
                  }}
                >
                  <MenuItem value={5}>5-Point Scoring</MenuItem>
                  <MenuItem value={10}>10-Point Scoring</MenuItem>
                </Select>
              </FormControl>

              {/* Role-Based Matrix */}
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
                sx={{ marginBottom: "10px" }}
              />
            </Box>

            {/* Right Column: Data Entry Assignment Method & Anonymous Entry */}
            <Box sx={{ flex: 1, display: "flex", flexDirection: "column" }}>
              {/* Data Entry Assignment Method Label */}
              <FormControl sx={{ width: "120%" }}>
                <FormLabel id="data-entry-label">
                  Data Entry Assignment Method
                </FormLabel>
                <Select
                  labelId="data-entry-label"
                  id="data-entry-select"
                  value={randomAssigned}
                  onChange={dataEntryHandleChange}
                  label="Data Entry"
                  sx={{
                    "& .MuiInputBase-input": {
                      color: "black", // Text color inside the Select box
                    },
                    "& .MuiSelect-icon": {
                      color: "black",
                    },
                    border: "1px solid lightgray",
                    borderRadius: "20px",
                    marginBottom: "10px",
                  }}
                >
                  <MenuItem value={"random"}>Random</MenuItem>
                  <MenuItem value={"assigned"}>Assigned</MenuItem>
                </Select>
              </FormControl>
            </Box>
          </Box>
        </Box>

        {/* This Box is for the Global Category Multipliers */}
        <Box
          id="GlobalCategoryMultipliersBox"
          sx={{
            backgroundColor: "white",
            width: "80%",
            height: "90%",
            position: "center",
            display: "flex",
            alignItems: "flex-start",
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

      {/* Space between the Boxes */}
      <Box sx={{ width: "10px" }} />

      <Box
        id="targetsBox"
        sx={{
          backgroundColor: "white",
          p: 2,
          borderRadius: 2,
          width: "21%",
          height: "85%",
          boxShadow: 3,
          position: "center",
          top: "10px",
          display: "flex",
          alignItems: "flex-start",
          flexDirection: "column",
          overflow: "auto",
        }}
      >
        {" "}
        <Box
          sx={{
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            mb: 2,
            width: "100%",
            marginBottom: "40px",
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
              width: "100%",
              gap: 1,
            }}
          >
            <TextField
              value={target}
              onChange={(e) => handleTargetChange(index, e)}
              variant="outlined"
              fullWidth
              placeholder="Enter target..."
              sx={{
                mb: 1,
                border: "1px solid #ccc",
                borderRadius: "20px",
                input: { color: "black" },
              }}
            />
            <IconButton
              onClick={() => handleDeleteTarget(index)}
              sx={{ mb: 1 }}
            >
              <DeleteIcon color="error" />
            </IconButton>
          </Box>
        ))}
      </Box>

      <Box sx={{ width: "10px" }} />

      {/* This Box is the Right-Half of the screen and it deals with managing participants */}
      <Box
        id="manageParticipantsBox"
        sx={{
          backgroundColor: "white",
          p: 2,
          borderRadius: 2,
          width: "21%",
          height: "85%",
          boxShadow: 3,
          position: "center",
          top: "10px",
          display: "flex",
          alignItems: "flex-start",
          flexDirection: "column",
          overflow: "auto",
        }}
      >
        {/* Container for Typography and Button */}
        <Box
          sx={{
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            width: "100%",
            marginBottom: "40px",
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

        {/* Participant Rows */}
        {participantsData.map((participant, index) => (
          <Paper
            key={index}
            elevation={0}
            sx={{
              color: "black",
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              p: 2,
              gap: 4,
              border: "1px solid #ccc",
              borderRadius: "20px",
              backgroundColor: "white",
              mb: 1,
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
  );
};

export default CreateMatrix;
